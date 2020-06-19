package com.bonree.brfs.duplication.datastream.file;

import com.bonree.brfs.common.collection.SortedList;
import com.bonree.brfs.common.timer.TimeExchangeEventEmitter;
import com.bonree.brfs.common.timer.TimeExchangeListener;
import com.bonree.brfs.common.utils.PooledThreadFactory;
import com.bonree.brfs.configuration.Configs;
import com.bonree.brfs.configuration.units.RegionNodeConfigs;
import com.bonree.brfs.duplication.datastream.file.sync.FileObjectSyncCallback;
import com.bonree.brfs.duplication.datastream.file.sync.FileObjectSynchronizer;
import com.bonree.brfs.duplication.filenode.FileNode;
import com.bonree.brfs.duplication.filenode.FileNodeSink;
import com.bonree.brfs.duplication.filenode.FileNodeSinkManager;
import com.bonree.brfs.duplication.filenode.FileNodeSinkManager.StateListener;
import com.bonree.brfs.duplication.storageregion.StorageRegion;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFileObjectSupplier implements FileObjectSupplier, TimeExchangeListener, FileNodeSink {
    private static Logger log = LoggerFactory.getLogger(DefaultFileObjectSupplier.class);

    private final FileObjectFactory fileFactory;

    private final ExecutorService mainThread;

    private final int cleanLimit;
    private final int forceCleanLimit;
    private final double cleanFileLengthRatio;

    //每个文件只会处于下列状态中的一个
    private final SortedList<FileObject> idleFileList = new SortedList<FileObject>(FileObject.LENGTH_COMPARATOR);
    private final SortedList<FileObject> busyFileList = new SortedList<FileObject>(FileObject.LENGTH_COMPARATOR);
    private final SortedList<FileObject> exceptionFileList = new SortedList<FileObject>(FileObject.LENGTH_COMPARATOR);

    private final List<FileObject> recycledFiles = Collections.synchronizedList(new ArrayList<FileObject>());
    private final List<FileObject> exceptedFiles = Collections.synchronizedList(new ArrayList<FileObject>());

    private final Object waitObj = new Object();

    private final FileObjectCloser fileCloser;
    private final FileObjectSynchronizer fileSynchronizer;
    private final FileNodeSinkManager fileNodeSinkManager;

    private final TimeExchangeEventEmitter timeEventEmitter;
    private volatile long expiredTime;

    private final StorageRegion storageRegion;
    private volatile boolean closed = false;

    public DefaultFileObjectSupplier(StorageRegion storageRegion,
                                     FileObjectFactory factory,
                                     FileObjectCloser closer,
                                     FileObjectSynchronizer fileSynchronizer,
                                     FileNodeSinkManager fileNodeSinkManager,
                                     TimeExchangeEventEmitter timeEventEmitter) {
        this(storageRegion, factory, closer, fileSynchronizer, fileNodeSinkManager, timeEventEmitter,
             Configs.getConfiguration().getConfig(RegionNodeConfigs.CONFIG_FILE_CLEAN_COUNT),
             Configs.getConfiguration().getConfig(RegionNodeConfigs.CONFIG_MAX_FILE_COUNT),
             Configs.getConfiguration().getConfig(RegionNodeConfigs.CONFIG_FILE_CLEAN_USAGE_RATE));
    }

    public DefaultFileObjectSupplier(StorageRegion storageRegion,
                                     FileObjectFactory factory,
                                     FileObjectCloser closer,
                                     FileObjectSynchronizer fileSynchronizer,
                                     FileNodeSinkManager fileNodeSinkManager,
                                     TimeExchangeEventEmitter timeEventEmitter,
                                     int cleanLimit,
                                     int forceCleanLimit,
                                     double cleanFileLengthRatio) {
        this.storageRegion = storageRegion;
        this.fileFactory = factory;
        this.fileCloser = closer;
        this.fileSynchronizer = fileSynchronizer;
        this.fileNodeSinkManager = fileNodeSinkManager;
        this.timeEventEmitter = timeEventEmitter;
        this.cleanLimit = Math.min(cleanLimit, forceCleanLimit);
        this.forceCleanLimit = forceCleanLimit;
        this.cleanFileLengthRatio = cleanFileLengthRatio;
        this.mainThread = Executors.newSingleThreadExecutor(new PooledThreadFactory(storageRegion.getName() + "_file_supplier"));

        this.expiredTime = timeEventEmitter.getStartTime(Duration.parse(storageRegion.getFilePartitionDuration()));
        this.timeEventEmitter.addListener(Duration.parse(storageRegion.getFilePartitionDuration()), this);
        this.fileNodeSinkManager.registerFileNodeSink(this);
        this.fileNodeSinkManager.addStateListener(stateListener);
    }

    private int totalSize() {
        return idleFileList.size() + busyFileList.size();
    }

    @Override
    public FileObject fetch(int size) throws InterruptedException {
        if (closed) {
            log.error("file Object supplier[{}] is already closed", storageRegion.getName());
            return null;
        }

        try {
            Future<FileObject> future = mainThread.submit(new FileFetcher(size));
            return future.get();
        } catch (ExecutionException e) {
            log.error("fetch file failed", e.getCause());
        }

        return null;
    }

    @Override
    public void recycle(FileObject file, boolean needSync) {
        if (file.getState() == FileObject.STATE_ABANDON) {
            return;
        }

        if (file.getState() == FileObject.STATE_CLOSING) {
            fileCloser.close(file, true);
            return;
        }

        try {
            if (needSync) {
                log.info("error occurred in file[{}]", file.node().getName());
                exceptedFiles.add(file);

                fileSynchronizer.synchronize(file, new FileObjectSyncCallback() {

                    @Override
                    public void complete(FileObject file, long fileLength) {
                        if (file.length() != fileLength) {
                            log.warn("update file[{}] length from [{}] to [{}]",
                                     file.node().getName(),
                                     file.length(),
                                     fileLength);
                            file.setLength(fileLength);
                        }

                        recycle(file, false);
                    }

                    @Override
                    public void timeout(FileObject file) {
                        log.info("file[{}] is timeout to sync, just close it");
                        fileCloser.close(file, false);
                    }
                });

                return;
            }

            if (file.node().getCreateTime() < expiredTime) {
                fileCloser.close(file, true);
                return;
            }

            recycledFiles.add(file);
        } finally {
            synchronized (waitObj) {
                waitObj.notifyAll();
            }
        }
    }

    private void recycleFileObjects() {
        synchronized (exceptedFiles) {
            Iterator<FileObject> iter = exceptedFiles.iterator();
            while (iter.hasNext()) {
                FileObject file = iter.next();
                busyFileList.remove(file);
                exceptionFileList.add(file);

                iter.remove();
            }
        }

        synchronized (recycledFiles) {
            Iterator<FileObject> iter = recycledFiles.iterator();
            while (iter.hasNext()) {
                FileObject file = iter.next();
                if (file.getState() == FileObject.STATE_ABANDON) {
                    continue;
                }

                if (file.node().getCreateTime() < expiredTime
                    || file.getState() == FileObject.STATE_CLOSING) {
                    fileCloser.close(file, true);
                    continue;
                }

                idleFileList.add(file);
                busyFileList.remove(file);
                exceptionFileList.remove(file);

                iter.remove();
            }
        }
    }

    private void clearList() {
        for (FileObject file : exceptionFileList) {
            file.setState(FileObject.STATE_CLOSING);
        }
        exceptionFileList.clear();

        for (FileObject file : busyFileList) {
            file.setState(FileObject.STATE_CLOSING);
        }
        busyFileList.clear();

        for (FileObject file : idleFileList) {
            fileCloser.close(file, true);
        }
        idleFileList.clear();

        exceptedFiles.clear();

        synchronized (recycledFiles) {
            Iterator<FileObject> iter = recycledFiles.iterator();
            while (iter.hasNext()) {
                FileObject file = iter.next();
                fileCloser.close(file, true);

                iter.remove();
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        log.info("sr[{}] file object supplier is closed", storageRegion.getName());
        fileNodeSinkManager.removeStateListener(stateListener);
        fileNodeSinkManager.unregisterFileNodeSink(this);
        timeEventEmitter.removeListener(Duration.parse(storageRegion.getFilePartitionDuration()), this);
        mainThread.submit(() -> clearList());
        mainThread.shutdown();
    }

    private class FileFetcher implements Callable<FileObject> {
        private int dataSize;

        public FileFetcher(int dataSize) {
            this.dataSize = dataSize;
        }

        private void checkSize(int size, FileObject file) {
            if (size > file.capacity()) {
                throw new IllegalStateException(
                    "data size is too large to save to file, get " + dataSize + ", but max " + file.capacity());
            }
        }

        @Override
        public FileObject call() throws Exception {
            while (true) {
                recycleFileObjects();

                Iterator<FileObject> iter = idleFileList.iterator();
                while (iter.hasNext()) {
                    FileObject file = iter.next();
                    if (file.apply(dataSize)) {
                        iter.remove();
                        busyFileList.add(file);
                        return file;
                    }

                    checkSize(dataSize, file);

                    if ((totalSize() >= cleanLimit && Double.compare(file.length(), file.capacity() * cleanFileLengthRatio) >= 0)
                        || (totalSize() >= forceCleanLimit)) {
                        log.info("force clean to file[{}]", file.node().getName());
                        iter.remove();
                        fileCloser.close(file, true);
                    }
                }

                List<FileObject> usableBusyFileList = new ArrayList<FileObject>();
                for (FileObject file : busyFileList) {
                    if (file.free() >= dataSize) {
                        usableBusyFileList.add(file);
                        continue;
                    }

                    checkSize(dataSize, file);
                }

                log.debug("idle => {}, busy => {}, exception => {}", idleFileList.size(), busyFileList.size(),
                          exceptionFileList.size());
                if (totalSize() < cleanLimit || (totalSize() < forceCleanLimit && usableBusyFileList.isEmpty())) {
                    FileObject file = fileFactory.createFile(storageRegion);
                    if (file == null) {
                        throw new RuntimeException("can not create file node!");
                    }

                    log.info("create file object[{}] with capactiy[{}]", file.node().getName(), file.capacity());
                    if (dataSize > file.capacity()) {
                        idleFileList.add(file);
                        throw new IllegalStateException(
                            "data size is too large to save to file, get " + dataSize + ", but max " + file.capacity());
                    }

                    file.apply(dataSize);
                    busyFileList.add(file);

                    return file;
                }

                log.debug("available busy file count => {}", usableBusyFileList.size());
                synchronized (waitObj) {
                    if (recycledFiles.isEmpty() && exceptedFiles.isEmpty()) {
                        waitObj.wait(1000);
                    }
                }
            }
        }

    }

    @Override
    public void timeExchanged(long startTime, Duration duration) {
        mainThread.submit(new Runnable() {

            @Override
            public void run() {
                expiredTime = startTime;
                log.info("Time[{}] to clear file list", new DateTime());
                clearList();
            }
        });
    }

    @Override
    public StorageRegion getStorageRegion() {
        return storageRegion;
    }

    @Override
    public void received(FileNode fileNode) {
        recycle(new FileObject(fileNode), true);
    }

    private FileNodeSinkManager.StateListener stateListener = new StateListener() {

        @Override
        public void stateChanged(boolean enable) {
            if (!enable) {
                mainThread.submit(new Runnable() {

                    @Override
                    public void run() {
                        idleFileList.clear();

                        for (FileObject file : busyFileList) {
                            file.setState(FileObject.STATE_ABANDON);
                        }

                        for (FileObject file : exceptionFileList) {
                            file.setState(FileObject.STATE_ABANDON);
                        }

                        busyFileList.clear();
                        exceptionFileList.clear();

                        recycledFiles.clear();
                        exceptedFiles.clear();
                    }
                });
            }
        }
    };
}

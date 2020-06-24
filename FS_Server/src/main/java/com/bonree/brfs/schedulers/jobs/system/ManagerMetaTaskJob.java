package com.bonree.brfs.schedulers.jobs.system;

import com.bonree.brfs.common.service.Service;
import com.bonree.brfs.common.service.ServiceManager;
import com.bonree.brfs.common.task.TaskType;
import com.bonree.brfs.common.utils.BrStringUtils;
import com.bonree.brfs.email.EmailPool;
import com.bonree.brfs.schedulers.ManagerContralFactory;
import com.bonree.brfs.schedulers.task.manager.MetaTaskManagerInterface;
import com.bonree.brfs.schedulers.task.operation.impl.QuartzOperationStateTask;
import com.bonree.brfs.schedulers.utils.JobDataMapConstract;
import com.bonree.mail.worker.MailWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerMetaTaskJob extends QuartzOperationStateTask {
    private static final Logger LOG = LoggerFactory.getLogger(ManagerMetaTaskJob.class);

    @Override
    public void interrupt() {

    }

    @Override
    public void operation(JobExecutionContext context) throws Exception {
        LOG.info("revise task work");
        JobDataMap data = context.getJobDetail().getJobDataMap();
        // 任务过期时间 ms
        ManagerContralFactory mcf = ManagerContralFactory.getInstance();
        long ttlTime = TimeUnit.SECONDS.toMillis(mcf.getTaskConfig().getTtlSecond());

        MetaTaskManagerInterface release = mcf.getTm();
        // 获取可用服务
        String groupName = mcf.getGroupName();
        ServiceManager sm = mcf.getSm();
        // 2.设置可用服务
        List<String> serverIds = getServerIds(sm, groupName);
        if (serverIds == null || serverIds.isEmpty()) {
            LOG.warn("available server list is null");
            return;
        }
        for (TaskType taskType : TaskType.values()) {
            try {
                release.reviseTaskStat(taskType.name(), ttlTime, serverIds);
            } catch (Exception e) {
                LOG.warn("{}", e.getMessage());
                EmailPool emailPool = EmailPool.getInstance();
                MailWorker.Builder builder = MailWorker.newBuilder(emailPool.getProgramInfo());
                builder.setModel(this.getClass().getSimpleName() + "模块服务发生问题");
                builder.setException(e);
                builder.setMessage("管理任务数据发生错误");
                builder.setVariable(data.getWrappedMap());
                emailPool.sendEmail(builder);
            }
        }
        LOG.info("revise task success !!!");
    }

    /***
     * 概述：获取存活的serverid
     * @param sm
     * @param groupName
     * @return
     * @user <a href=mailto:zhucg@bonree.com>朱成岗</a>
     */
    private List<String> getServerIds(ServiceManager sm, String groupName) {
        List<String> sids = new ArrayList<>();
        List<Service> services = sm.getServiceListByGroup(groupName);
        if (services == null || services.isEmpty()) {
            return sids;
        }
        String sid = null;
        for (Service server : services) {
            sid = server.getServiceId();
            if (BrStringUtils.isEmpty(sid)) {
                continue;
            }
            sids.add(sid);
        }
        return sids;
    }

}

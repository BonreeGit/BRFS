package com.bonree.brfs.common.resource.vo;

import com.google.common.base.MoreObjects;

/*******************************************************************************
 * 版权信息： 北京博睿宏远数据科技股份有限公司
 * Copyright (c) 2007-2020 北京博睿宏远数据科技股份有限公司，Inc. All Rights Reserved.
 * @date 2020年03月24日 16:20:58
 * @author: <a href=mailto:zhucg@bonree.com>朱成岗</a>
 * @description: 磁盘分区内部消息 用途对比判断磁盘的挂载点，设备名称，大小是否存在变化，若有变化则认为磁盘发生变更
 ******************************************************************************/

public class LocalPartitionInfo {


    private String partitionGroup;
    private String partitionId;
    private String devName;
    private String mountPoint;
    private String dataDir;
    private PartitionType type = PartitionType.NORMAL;
    private long totalSize;

    public LocalPartitionInfo() {
    }

    public LocalPartitionInfo(String partitionId, String devName, String mountPoint, String dataDir, long totalSize) {
        this.partitionId = partitionId;
        this.devName = devName;
        this.mountPoint = mountPoint;
        this.dataDir = dataDir;
        this.totalSize = totalSize;
    }

    public String getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(String partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public double getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public PartitionType getType() {
        return type;
    }

    public void setType(PartitionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("partitionGroup", partitionGroup)
                          .add("partitionId", partitionId)
                          .add("devName", devName)
                          .add("mountPoint", mountPoint)
                          .add("dataDir", dataDir)
                          .add("type", type)
                          .add("totalSize", totalSize)
                          .toString();
    }
}
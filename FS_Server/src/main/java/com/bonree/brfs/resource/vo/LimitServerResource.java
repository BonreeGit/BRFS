package com.bonree.brfs.resource.vo;

import com.bonree.brfs.configuration.Configs;
import com.bonree.brfs.configuration.units.CommonConfigs;
import com.bonree.brfs.configuration.units.DataNodeConfigs;
import com.bonree.brfs.configuration.units.ResourceConfigs;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitServerResource {

    private long remainWarnSize = Configs.getConfiguration().getConfig(ResourceConfigs.CONFIG_LIMIT_DISK_REMAIN_SIZE);
    private long remainForceSize = Configs.getConfiguration().getConfig(ResourceConfigs.CONFIG_LIMIT_FORCE_DISK_REMAIN_SIZE);
    private int centSize = Configs.getConfiguration().getConfig(ResourceConfigs.CONFIG_RESOURCE_CENT_SIZE);
    private long fileSize = Configs.getConfiguration().getConfig(DataNodeConfigs.CONFIG_FILE_MAX_CAPACITY) / 1024;
    private String diskGroup = Configs.getConfiguration().getConfig(CommonConfigs.CONFIG_DATA_SERVICE_GROUP_NAME);

    public long getRemainWarnSize() {
        return remainWarnSize;
    }

    public void setRemainWarnSize(long remainWarnSize) {
        this.remainWarnSize = remainWarnSize;
    }

    public long getRemainForceSize() {
        return remainForceSize;
    }

    public void setRemainForceSize(long remainForceSize) {
        this.remainForceSize = remainForceSize;
    }

    public int getCentSize() {
        return centSize;
    }

    public void setCentSize(int centSize) {
        this.centSize = centSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDiskGroup() {
        return diskGroup;
    }

    public void setDiskGroup(String diskGroup) {
        this.diskGroup = diskGroup;
    }
}
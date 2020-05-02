package com.bonree.brfs.disknode;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceConfig {
    @JsonProperty("switch")
    private boolean runFlag = true;
    @JsonProperty("interval.seconds")
    private int intervalTime = 10;

    public boolean isRunFlag() {
        return runFlag;
    }

    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }
}

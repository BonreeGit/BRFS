package com.bonree.brfs.gui.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;

public class GuiResourceConfig {
    @JsonProperty("dir")
    private String guiDir = "gui";
    @JsonProperty("interval.seconds")
    private int intervalTime = 60;
    @JsonProperty("ttl.seconds")
    private int ttlTime = 7 * 24 * 60 * 60;
    @JsonProperty("maintain.interval.seconds")
    private int scanIntervalTime = 60;

    public GuiResourceConfig() {
    }

    public String getGuiDir() {
        return guiDir;
    }

    public void setGuiDir(String guiDir) {
        this.guiDir = guiDir;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public int getTtlTime() {
        return ttlTime;
    }

    public void setTtlTime(int ttlTime) {
        this.ttlTime = ttlTime;
    }

    public int getScanIntervalTime() {
        return scanIntervalTime;
    }

    public void setScanIntervalTime(int scanIntervalTime) {
        this.scanIntervalTime = scanIntervalTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GuiResourceConfig{");
        sb.append("guiDir='").append(guiDir).append('\'');
        sb.append(", intervalTime=").append(intervalTime);
        sb.append(", ttlTime=").append(ttlTime);
        sb.append(", scanIntervalTime=").append(scanIntervalTime);
        sb.append('}');
        return sb.toString();
    }
}
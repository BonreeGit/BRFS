package com.bonree.brfs.schedulers.task.model;

import com.bonree.brfs.common.task.TaskState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;

/*****************************************************************************
 * 版权信息：北京博睿宏远数据科技股份有限公司
 * Copyright: Copyright (c) 2007北京博睿宏远数据科技股份有限公司,Inc.All Rights Reserved.
 *
 * @date 2018年4月13日 下午2:55:12
 * @Author: <a href=mailto:zhucg@bonree.com>朱成岗</a>
 * @Description: 任务服务节点
 *****************************************************************************
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskServerNodeModel {
    @JsonProperty("taskStartTime")
    private String taskStartTime;
    @JsonProperty("taskStopTime")
    private String taskStopTime;
    @JsonProperty("taskState")
    private int taskState = TaskState.INIT.code();
    @JsonProperty("retryCount")
    private int retryCount = 0;
    @JsonProperty("atomTaskModels")
    private List<AtomTaskModel> atomTaskModels = new ArrayList<AtomTaskModel>();
    @JsonProperty("result")
    private TaskResultModel result;

    public TaskResultModel getResult() {
        return result;
    }

    public void setResult(TaskResultModel result) {
        this.result = result;
    }

    public int getTaskState() {
        return taskState;
    }

    public void setTaskState(int taskState) {
        this.taskState = taskState;
    }

    public List<AtomTaskModel> getAtomTaskModels() {
        return atomTaskModels;
    }

    public void setAtomTaskModels(List<AtomTaskModel> atomTaskModels) {
        this.atomTaskModels = atomTaskModels;
    }

    public void addAll(List<AtomTaskModel> atomTaskModels) {
        if (this.atomTaskModels == null) {
            this.atomTaskModels = new ArrayList<>();
        }
        this.atomTaskModels.addAll(atomTaskModels);
    }

    public void add(AtomTaskModel atom) {
        if (this.atomTaskModels == null) {
            this.atomTaskModels = new ArrayList<>();
        }
        this.atomTaskModels.add(atom);
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(String taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public String getTaskStopTime() {
        return taskStopTime;
    }

    public void setTaskStopTime(String taskStopTime) {
        this.taskStopTime = taskStopTime;
    }

    /**
     * 概述：获取初始化对象
     *
     * @return
     *
     * @user <a href=mailto:zhucg@bonree.com>朱成岗</a>
     */
    public static TaskServerNodeModel getInitInstance() {
        TaskServerNodeModel task = new TaskServerNodeModel();
        task.setTaskState(TaskState.INIT.code());
        return task;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("taskStartTime", taskStartTime)
                          .add("taskStopTime", taskStopTime)
                          .add("taskState", taskState)
                          .add("retryCount", retryCount)
                          .add("atomTaskModels", atomTaskModels)
                          .add("result", result)
                          .toString();
    }
}

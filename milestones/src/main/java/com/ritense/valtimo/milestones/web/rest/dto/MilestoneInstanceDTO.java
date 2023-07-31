/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.milestones.web.rest.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class MilestoneInstanceDTO {

    private Long id;
    private String title;
    private String processDefinitionKey;
    private String taskDefinitionKey;
    private Integer plannedIntervalInDays;
    private String processInstanceId;
    private LocalDate referenceDate;
    private boolean reached;
    private Boolean reachedInTime;
    private LocalDate expectedDate;
    private ZonedDateTime reachedDate;

    public MilestoneInstanceDTO() {
        //Default constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public Integer getPlannedIntervalInDays() {
        return plannedIntervalInDays;
    }

    public void setPlannedIntervalInDays(Integer plannedIntervalInDays) {
        this.plannedIntervalInDays = plannedIntervalInDays;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public boolean isReached() {
        return reached;
    }

    public void setReached(boolean reached) {
        this.reached = reached;
    }

    public Boolean getReachedInTime() {
        return reachedInTime;
    }

    public void setReachedInTime(Boolean reachedInTime) {
        this.reachedInTime = reachedInTime;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public ZonedDateTime getReachedDate() {
        return reachedDate;
    }

    public void setReachedDate(ZonedDateTime reachedDate) {
        this.reachedDate = reachedDate;
    }
}

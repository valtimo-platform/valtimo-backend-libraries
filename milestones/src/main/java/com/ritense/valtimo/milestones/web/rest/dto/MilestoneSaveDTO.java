/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import javax.validation.constraints.NotNull;

public class MilestoneSaveDTO {

    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String processDefinitionKey;

    @NotNull
    private String taskDefinitionKey;

    @NotNull
    private Integer plannedIntervalInDays;

    private String color;

    @NotNull
    private Long milestoneSet;

    @SuppressWarnings("squid:S2637")
    public MilestoneSaveDTO() {
        // Empty constructor that is used by Jackson to create the DTO
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Long getMilestoneSet() {
        return milestoneSet;
    }

    public void setMilestoneSet(Long milestoneSet) {
        this.milestoneSet = milestoneSet;
    }

    @Override
    public String toString() {
        return "MilestoneSaveDTO{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", milestoneSet=" + milestoneSet +
            '}';
    }
}

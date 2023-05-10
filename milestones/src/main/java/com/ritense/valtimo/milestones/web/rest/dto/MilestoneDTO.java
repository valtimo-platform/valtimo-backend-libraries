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

import com.ritense.valtimo.milestones.domain.MilestoneSet;

public class MilestoneDTO {

    private Long id;
    private String title;
    private String color;
    private Integer plannedIntervalInDays;
    private String processDefinitionKey;
    private String taskDefinitionKey;
    private MilestoneSet milestoneSet;

    public MilestoneDTO() {
    }

    public MilestoneDTO(
            Long id,
            String title,
            String color,
            Integer plannedIntervalInDays,
            String processDefinitionKey,
            String taskDefinitionKey,
            MilestoneSet milestoneSet) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.plannedIntervalInDays = plannedIntervalInDays;
        this.processDefinitionKey = processDefinitionKey;
        this.taskDefinitionKey = taskDefinitionKey;
        this.milestoneSet = milestoneSet;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getPlannedIntervalInDays() {
        return plannedIntervalInDays;
    }

    public void setPlannedIntervalInDays(Integer plannedIntervalInDays) {
        this.plannedIntervalInDays = plannedIntervalInDays;
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

    public MilestoneSet getMilestoneSet() {
        return milestoneSet;
    }

    public void setMilestoneSet(MilestoneSet milestoneSet) {
        this.milestoneSet = milestoneSet;
    }

    @Override
    public String toString() {
        return "MilestoneDTO{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", color='" + color + '\'' +
            ", plannedIntervalInDays=" + plannedIntervalInDays +
            ", processDefinitionKey='" + processDefinitionKey + '\'' +
            ", taskDefinitionKey='" + taskDefinitionKey + '\'' +
            ", milestoneSet=" + milestoneSet +
            '}';
    }
}

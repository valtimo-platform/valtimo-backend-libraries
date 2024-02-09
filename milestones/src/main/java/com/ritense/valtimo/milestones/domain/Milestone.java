/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.milestones.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

@Entity
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String processDefinitionKey;

    @NotNull
    private String taskDefinitionKey;

    @NotNull
    private Integer plannedIntervalInDays;

    @NotNull
    @Range(min = 0, max = 0xffffff)
    private int color;

    @NotNull
    @ManyToOne
    private MilestoneSet milestoneSet;

    @SuppressWarnings({"squid:S2637", "java:S2637"})
    public Milestone() {
    }

    @SuppressWarnings({"squid:S2637", "java:S2637"})
    public Milestone(
        String title,
        String processDefinitionKey,
        String taskDefinitionKey,
        Integer plannedIntervalInDays,
        String color,
        MilestoneSet milestoneSet
    ) {
        this.title = title;
        this.processDefinitionKey = processDefinitionKey;
        this.taskDefinitionKey = taskDefinitionKey;
        this.plannedIntervalInDays = plannedIntervalInDays;
        this.setColor(color);
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
        return String.format("#%06x", color);
    }

    public void setColor(String color) {
        String colorHex = color.substring(1);
        String newColorHex = "";
        if (colorHex.length() == 3) {
            newColorHex += colorHex.charAt(0);
            newColorHex += colorHex.charAt(0);
            newColorHex += colorHex.charAt(1);
            newColorHex += colorHex.charAt(1);
            newColorHex += colorHex.charAt(2);
            newColorHex += colorHex.charAt(2);
        } else {
            newColorHex = colorHex;
        }

        this.color = Integer.parseInt(newColorHex, 16);
    }

    public MilestoneSet getMilestoneSet() {
        return milestoneSet;
    }

    public void setMilestoneSet(MilestoneSet milestoneSet) {
        this.milestoneSet = milestoneSet;
    }
}

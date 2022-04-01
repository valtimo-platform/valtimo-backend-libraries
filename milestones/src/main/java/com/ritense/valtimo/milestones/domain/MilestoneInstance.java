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

package com.ritense.valtimo.milestones.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Range;
import org.jvnet.hk2.annotations.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
public class MilestoneInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @NotNull
    @ManyToOne
    private MilestoneSet milestoneSet;

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
    private String processInstanceId;

    @Column(columnDefinition = "DATE")
    @NotNull
    private LocalDate referenceDate;

    @NotNull
    private boolean reached;

    @Optional
    private Boolean reachedInTime;

    @Column(columnDefinition = "DATE")
    @NotNull
    private LocalDate expectedDate;

    @Column(columnDefinition = "DATE")
    @Optional
    private ZonedDateTime reachedDate;

    public MilestoneInstance(){
    }

    public static MilestoneInstance create(
            MilestoneSet milestoneSet,
            String title,
            String processDefinitionKey,
            String taskDefinitionKey,
            Integer plannedIntervalInDays,
            String color,
            String processInstanceId,
            LocalDate referenceDate,
            boolean reached,
            Boolean reachedInTime,
            ZonedDateTime reachedDate) {
        MilestoneInstance milestone = new MilestoneInstance();

        milestone.milestoneSet = milestoneSet;
        milestone.title = title;
        milestone.processDefinitionKey = processDefinitionKey;
        milestone.taskDefinitionKey = taskDefinitionKey;
        milestone.plannedIntervalInDays = plannedIntervalInDays;
        milestone.setColor(color);
        milestone.processInstanceId = processInstanceId;
        milestone.referenceDate = referenceDate;
        milestone.reached = reached;
        milestone.reachedInTime = reachedInTime;
        milestone.reachedDate = reachedDate;

        return milestone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MilestoneSet getMilestoneSet() {
        return milestoneSet;
    }

    public void setMilestoneSet(MilestoneSet milestoneSet) {
        this.milestoneSet = milestoneSet;
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
        if (colorHex.length() == 3) {
            colorHex = colorHex.replaceAll(".", "$0$0");
        }

        this.color = Integer.parseInt(colorHex, 16);
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

    private void setReached(boolean reached) {
        this.reached = reached;
    }

    public Boolean getReachedInTime() {
        return reachedInTime;
    }

    private void setReachedInTime(Boolean reachedInTime) {
        this.reachedInTime = reachedInTime;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    private void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public ZonedDateTime getReachedDate() {
        return reachedDate;
    }

    private void setReachedDate(ZonedDateTime reachedDate) {
        this.reachedDate = reachedDate;
    }

    public void calculateExpectedDate() {
        this.expectedDate = this.referenceDate.plusDays(this.plannedIntervalInDays);
    }

    public void completeMilestone(ZonedDateTime reachDate) {
        setReached(true);
        setReachedDate(reachDate);
        determineAndSetReachedInTime();
    }

    public void changeReferenceDateByOffset(Long offsetInDays) {
        setReferenceDate(this.referenceDate.plusDays(offsetInDays));
        setExpectedDate(this.expectedDate.plusDays(offsetInDays));
        if (isReached()) {
            determineAndSetReachedInTime();
        }
    }

    private void determineAndSetReachedInTime() {
        // Used "is not after" instead of "is before" so when the dates are equal, it is also marked as reached in time.
        setReachedInTime(!reachedDate.toLocalDate().isAfter(getExpectedDate()));
    }
}
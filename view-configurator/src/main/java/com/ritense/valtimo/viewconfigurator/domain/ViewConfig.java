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

package com.ritense.valtimo.viewconfigurator.domain;

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents configuration of process variables and their labels of Views in the console.
 */
@Entity
@Table(name = "view_config")
public class ViewConfig {

    @JsonView(Views.ViewConfig.class)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "view_config_id")
    private Long id;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "process_definition_id", nullable = false)
    private String processDefinitionId;

    @JsonView(Views.ViewConfig.class)
    @Fetch(FetchMode.JOIN)
    @OneToMany(targetEntity = View.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "view_config_id")
    private Set<View> views = new HashSet<>();

    @JsonView(Views.ViewConfig.class)
    @Fetch(FetchMode.JOIN)
    @OneToMany(targetEntity = ProcessDefinitionVariable.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "view_config_id")
    private Set<ProcessDefinitionVariable> allProcessDefinitionVariables = new HashSet<>();

    private ViewConfig() {
    }

    public ViewConfig(String processDefinitionId, Set<ProcessDefinitionVariable> allProcessDefinitionVariables, Set<View> views) {
        Objects.requireNonNull(processDefinitionId, "processDefinitionId cannot be null");
        if (processDefinitionId.isEmpty()) {
            throw new IllegalArgumentException("processDefinitionId must be provided");
        }

        Objects.requireNonNull(allProcessDefinitionVariables, "processDefinitionVariables cannot be null");
        if (allProcessDefinitionVariables.isEmpty()) {
            throw new IllegalArgumentException("Must supply at least one processDefinitionVariable");
        }

        Objects.requireNonNull(views, "views cannot be null");
        if (views.isEmpty()) {
            throw new IllegalArgumentException("Must supply at least one view");
        }

        this.processDefinitionId = processDefinitionId;
        this.views.addAll(views);
        this.allProcessDefinitionVariables.addAll(allProcessDefinitionVariables);
    }

    public void configureFrom(ViewConfig otherViewConfig) {
        Objects.requireNonNull(otherViewConfig, "otherViewConfig cannot be null");

        Set<ProcessDefinitionVariable> variablesAvailableForCopy = determineVariablesAvailableForCopy(otherViewConfig);

        this.views.stream().forEach(view -> {
            otherViewConfig.getViews().stream().filter(otherView -> otherView.equivalentOf(view)).findFirst().ifPresent(
                otherView -> view.configureFrom(otherView, variablesAvailableForCopy)
            );
        });
    }

    public void changeLabels(Set<ProcessDefinitionVariable> variables) {
        variables.forEach(processDefinitionVariable -> allProcessDefinitionVariables
                .stream()
                .filter(p -> p.getId() != null && p.getId().equals(processDefinitionVariable.getId()))
                .findFirst()
                .ifPresent(p -> p.changeLabel(processDefinitionVariable.getLabel()))
        );
    }

    public void assignVariablesToView(Long viewId, LinkedHashSet<Long> variables) {
        final LinkedHashSet<ProcessDefinitionVariable> viewVars = variables
                .stream()
                .map(id -> allProcessDefinitionVariables
                    .stream()
                    .filter(p -> p.getId() != null && p.getId().equals(id))
                    .findFirst()
                    .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        views.stream()
            .filter(v -> v.getId() != null && v.getId().equals(viewId))
            .findFirst()
            .ifPresent(v -> v.assignVariables(viewVars));
    }

    public void assignGroupsToView(Long viewId, List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups) {
        Optional<View> viewOptional = views.stream()
            .filter(v -> v.getId() != null && v.getId().equals(viewId))
            .findFirst();

        if (viewOptional.isPresent()) {
            View view = viewOptional.get();
            if (view.supportsGroups()) {
                view.assignGroups(allProcessDefinitionVariables, viewConfigurationRequestGroups);
            } else {
                throw new IllegalArgumentException("View does not support groups");
            }
        } else {
            throw new NullPointerException("View was not found for view config");
        }
    }

    public void addView(View view) {
        if (!this.views.add(view)) {
            throw new IllegalStateException("View was not added.");
        }
    }

    public void assignAdditionalProcessVariables(Set<ProcessDefinitionVariable> additionals) {
        final Set<ProcessDefinitionVariable> combinedVars = additionals
                .stream()
                .map(additional -> allProcessDefinitionVariables
                        .stream()
                        .filter(hasReferenceIdMatchWith(additional))
                        .findFirst()
                        .orElse(additional))
                .collect(Collectors.toSet());
        this.allProcessDefinitionVariables.addAll(combinedVars);
    }

    private Set<ProcessDefinitionVariable> determineVariablesAvailableForCopy(ViewConfig otherViewConfig) {
        return this.allProcessDefinitionVariables.stream()
            .filter(thisViewConfigVariable -> otherViewConfig.getAllProcessDefinitionVariables().stream()
                .anyMatch(otherViewConfigVariable -> haveMatchingReferenceIdAndType(thisViewConfigVariable, otherViewConfigVariable))
            )
            .collect(Collectors.toSet());
    }

    private boolean haveMatchingReferenceIdAndType(ProcessDefinitionVariable otherViewConfigVariable, ProcessDefinitionVariable thisViewConfigVariable) {
        return thisViewConfigVariable.getReferenceId().equalsIgnoreCase(otherViewConfigVariable.getReferenceId())
            && thisViewConfigVariable.getTypeName().equalsIgnoreCase(otherViewConfigVariable.getTypeName());
    }

    private static Predicate<ProcessDefinitionVariable> hasReferenceIdMatchWith(ProcessDefinitionVariable processDefinitionVariable) {
        return p -> p.getReferenceId().equalsIgnoreCase(processDefinitionVariable.getReferenceId());
    }

    public Long getId() {
        return id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public Set<View> getViews() {
        return Collections.unmodifiableSet(views);
    }

    public Set<ProcessDefinitionVariable> getAllProcessDefinitionVariables() {
        return Collections.unmodifiableSet(allProcessDefinitionVariables);
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ViewConfig)) {
            return false;
        }
        ViewConfig that = (ViewConfig) o;
        return Objects.equals(getId(), that.getId())
            && Objects.equals(getProcessDefinitionId(), that.getProcessDefinitionId())
            && Objects.equals(getViews(), that.getViews())
            && Objects.equals(getAllProcessDefinitionVariables(), that.getAllProcessDefinitionVariables());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProcessDefinitionId(), getViews(), getAllProcessDefinitionVariables());
    }

    public static ViewConfig initialise(String processDefinitionId, Set<ProcessDefinitionVariable> processDefinitionVariables) {

        LinkedHashSet<ProcessDefinitionVariable> defaultProcessDefinitionVariables = DefaultProcessVarFactory.getDefaultProcessVariables();
        ViewVarGroup defaultProcessVariablesGroup = DefaultProcessVarFactory.getDefaultProcessVariablesGroup(defaultProcessDefinitionVariables);

        Set<View> views = ViewFactory.getDefaultViews(defaultProcessDefinitionVariables, defaultProcessVariablesGroup);

        processDefinitionVariables.addAll(defaultProcessDefinitionVariables);

        return new ViewConfig(processDefinitionId, processDefinitionVariables, views);
    }

}
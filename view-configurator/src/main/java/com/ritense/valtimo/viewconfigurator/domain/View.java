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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "view")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "view_type", discriminatorType = DiscriminatorType.STRING)
public abstract class View implements Serializable {

    @JsonView(Views.ViewConfig.class)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "view_id")
    private Long id;

    @JsonView(Views.ViewConfig.class)
    @JsonProperty("groups")
    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "view", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<ViewVarGroup> processDefinitionVariableGroups = new TreeSet<>();

    @JsonView(Views.ViewConfig.class)
    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "view", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<ViewVar> selectedProcessDefinitionVariables = new TreeSet<>();

    public View() {
    }

    public void assignVariables(LinkedHashSet<ProcessDefinitionVariable> variables) {
        selectedProcessDefinitionVariables.clear();
        int sequence = 1;
        for (ProcessDefinitionVariable variable : variables) {
            ViewVar viewVar = new ViewVar(this, variable, sequence);
            selectedProcessDefinitionVariables.add(viewVar);
            sequence++;
        }
    }

    public void assignGroups(Set<ProcessDefinitionVariable> processDefinitionVariables, List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups) {
        processDefinitionVariableGroups.clear();
        for (ViewConfigurationRequestGroup viewConfigurationRequestGroup : viewConfigurationRequestGroups) {
            processDefinitionVariableGroups.add(new ViewVarGroup(
                    this,
                    viewConfigurationRequestGroup.getLabel(),
                    viewConfigurationRequestGroup.getSequence(),
                    processDefinitionVariables,
                    viewConfigurationRequestGroup
            ));
        }
    }

    protected void configureFrom(View otherView, Set<ProcessDefinitionVariable> variablesAvailableForCopy) {
        configureVariablesFrom(otherView, variablesAvailableForCopy);
        configureGroupsFrom(otherView, variablesAvailableForCopy);
    }

    protected void addViewVarGroup(ViewVarGroup viewVarGroup) {
        viewVarGroup.setView(this);
        processDefinitionVariableGroups.add(viewVarGroup);
    }

    protected abstract boolean supportsGroups();

    protected abstract boolean equivalentOf(View otherView);

    private void configureVariablesFrom(View otherView, Set<ProcessDefinitionVariable> variablesAvailableForCopy) {
        selectedProcessDefinitionVariables.clear();
        AtomicInteger counter = new AtomicInteger(1);

        otherView.getSelectedProcessDefinitionVariables().stream()
            .sorted()
            .filter(viewVar -> variablesAvailableForCopy.contains(viewVar.getVar()))
            .map(viewVar -> {
                Optional<ProcessDefinitionVariable> availableVariable = variablesAvailableForCopy
                    .stream()
                    .filter(variable -> variable.equals(viewVar.getVar()))
                    .findFirst();
                return new ViewVar(this, availableVariable.get(), counter.getAndIncrement());
            })
            .forEach(selectedProcessDefinitionVariables::add);
    }

    private void configureGroupsFrom(View otherView, Set<ProcessDefinitionVariable> variablesAvailableForCopy) {
        processDefinitionVariableGroups.clear();

        AtomicInteger counter = new AtomicInteger(1);
        otherView.getProcessDefinitionVariableGroups().stream()
            .sorted()
            .map(viewVarGroup -> {
                ViewVarGroup group = new ViewVarGroup(this, viewVarGroup.getLabel(), counter.getAndIncrement());
                group.configureFrom(viewVarGroup, variablesAvailableForCopy);
                return group;
            })
            .forEach(processDefinitionVariableGroups::add);
    }

    public Set<ViewVar> getSelectedProcessDefinitionVariables() {
        return Collections.unmodifiableSet(selectedProcessDefinitionVariables);
    }

    public Set<ViewVarGroup> getProcessDefinitionVariableGroups() {
        return Collections.unmodifiableSet(processDefinitionVariableGroups);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof View)) {
            return false;
        }
        View view = (View) o;
        return
            Objects.equals(getId(), view.getId())
            &&
            Objects.equals(getSelectedProcessDefinitionVariables(), view.getSelectedProcessDefinitionVariables());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSelectedProcessDefinitionVariables());
    }
}
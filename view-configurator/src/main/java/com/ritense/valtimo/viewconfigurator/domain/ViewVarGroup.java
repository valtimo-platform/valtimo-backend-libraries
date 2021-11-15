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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Entity
@Table(name = "view_var_group")
public class ViewVarGroup implements Comparable<ViewVarGroup> {

    @JsonView(Views.ViewConfig.class)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "view_var_group_id")
    private Long id;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "label", columnDefinition = "varchar(255)")
    private String label;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "sequence", columnDefinition = "INT")
    private Integer sequence;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "view_id")
    private View view;

    @JsonView(Views.ViewConfig.class)
    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<GroupVar> selectedProcessDefinitionVariables = new TreeSet<>();

    protected ViewVarGroup() {
    }

    protected ViewVarGroup(View view, String label, Integer sequence) {
        this.view = view;
        this.label = label;
        this.sequence = sequence;
    }

    protected ViewVarGroup(String label, Integer sequence, LinkedHashSet<ProcessDefinitionVariable> selectedProcessDefinitionVariables) {
        this.label = label;
        this.sequence = sequence;
        this.assignVariables(selectedProcessDefinitionVariables);
    }

    public ViewVarGroup(View view, String label, Integer sequence, Set<ProcessDefinitionVariable> processDefinitionVariables, ViewConfigurationRequestGroup group) {
        this.view = view;
        this.label = label;
        this.sequence = sequence;
        this.assignVariables(processDefinitionVariables, group);
    }

    protected void configureFrom(ViewVarGroup otherViewVarGroup, Set<ProcessDefinitionVariable> variablesAvailableForCopy) {
        selectedProcessDefinitionVariables.clear();
        AtomicInteger counter = new AtomicInteger(1);

        otherViewVarGroup.getSelectedProcessDefinitionVariables().stream()
            .sorted()
            .filter(groupVar -> variablesAvailableForCopy.contains(groupVar.getVar()))
            .map(groupVar -> {
                Optional<ProcessDefinitionVariable> availableVariable = variablesAvailableForCopy.stream()
                    .filter(variable -> variable.equals(groupVar.getVar()))
                    .findFirst();
                return new GroupVar(this, availableVariable.get(), counter.getAndIncrement());
            })
            .forEach(selectedProcessDefinitionVariables::add);
    }

    public void assignVariables(Set<ProcessDefinitionVariable> processDefinitionVariables, ViewConfigurationRequestGroup group) {

        LinkedHashSet<ProcessDefinitionVariable> processDefinitionVariablesToAssign = group.getViewConfigurationRequestVariables().stream()
            .sorted()
            .map(variable -> {
                return processDefinitionVariables.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(variable.getId()))
                    .findFirst();
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        assignVariables(processDefinitionVariablesToAssign);
    }

    private void assignVariables(LinkedHashSet<ProcessDefinitionVariable> variables) {
        selectedProcessDefinitionVariables.clear();
        int sequence = 1;
        for (ProcessDefinitionVariable variable : variables) {
            GroupVar groupVar = new GroupVar(this, variable, sequence);
            selectedProcessDefinitionVariables.add(groupVar);
            sequence++;
        }
    }

    public Set<GroupVar> getSelectedProcessDefinitionVariables() {
        return Collections.unmodifiableSet(selectedProcessDefinitionVariables);
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public void setView(View view) {
        this.view = view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewVarGroup that = (ViewVarGroup) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(label, that.label) &&
            Objects.equals(sequence, that.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, sequence);
    }

    @Override
    public int compareTo(ViewVarGroup viewVarGroup) {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if (this == viewVarGroup) {
            return equal;
        }

        if (this.getSequence() < viewVarGroup.getSequence()) {
            return before;
        }
        if (this.getSequence() > viewVarGroup.getSequence()) {
            return after;
        }

        return equal;
    }
}

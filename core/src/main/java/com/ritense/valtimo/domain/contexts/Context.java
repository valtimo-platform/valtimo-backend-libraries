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

package com.ritense.valtimo.domain.contexts;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "context")
public class Context implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "context_id")
    private Long id;

    @Column(name = "name", length = 250, unique = true, nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(joinColumns = @JoinColumn(name = "context_id"))
    private Set<ContextProcess> processes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "context_roles", joinColumns = @JoinColumn(name = "context_id"))
    @Column(name = "role", length = 500)
    private Set<String> roles = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "context_id", nullable = false)
    private Set<MenuItem> menuItems = new HashSet<>();

    private Context() {
    }

    public Context(String name) {
        Objects.requireNonNull(name, "Context name cannot be null");
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addProcess(ContextProcess contextProcess) {
        Objects.requireNonNull(contextProcess, "contextProcess cannot be null");
        processes.add(contextProcess);
    }

    public void removeProcess(String processDefinitionKey) {
        Objects.requireNonNull(processDefinitionKey, "processDefinitionKey cannot be null");
        processes.removeIf(contextProcess -> contextProcess.getProcessDefinitionKey().equals(processDefinitionKey));
    }

    public boolean containsProcess(String processDefinitionKey) {
        Objects.requireNonNull(processDefinitionKey, "processDefinitionKey cannot be null");
        return processes.stream()
            .anyMatch(contextProcess -> contextProcess.getProcessDefinitionKey().equals(processDefinitionKey));
    }

    public boolean containsProcessAndVisibleInMenu(String processKey) {
        Objects.requireNonNull(processKey, "processKey cannot be null");
        return processes.stream()
            .anyMatch(contextProcess ->
                contextProcess.getProcessDefinitionKey().equals(processKey) && contextProcess.isVisibleInMenu()
            );
    }

    public Set<ContextProcess> getProcesses() {
        return processes;
    }

    public void addRole(String role) {
        Objects.requireNonNull(role, "role cannot be null");
        roles.add(role);
    }

    public void removeRole(String role) {
        Objects.requireNonNull(role, "role cannot be null");
        roles.remove(role);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean addMenuItem(MenuItem menuItem) {
        Objects.requireNonNull(menuItem, "menuItem cannot be null");
        return menuItems.add(menuItem);
    }

    public boolean removeMenuItem(MenuItem menuItem) {
        Objects.requireNonNull(menuItem, "menuItem cannot be null");
        return menuItems.remove(menuItem);
    }

    public Set<MenuItem> getMenuItems() {
        return menuItems;
    }

}
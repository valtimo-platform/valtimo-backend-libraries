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

package com.ritense.valtimo.contract.authentication.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is created to search/list users based on required and optionional user roles.
 * For instance, requiredUserGroup: role_1, orUserGroups: [role_2, role_3], [role_4, role_5] should list
 * all users that have role_1 and that have either (role_2 OR role_3) AND (role_4 OR role_5)
 */
public class SearchByUserGroupsCriteria {

    private Set<String> requiredUserGroups;
    private List<Set<String>> orUserGroups;

    public SearchByUserGroupsCriteria() {
        requiredUserGroups = new HashSet<>();
        orUserGroups = new ArrayList<>();
    }

    public SearchByUserGroupsCriteria(Set<String> requiredUserGroups) {
        this.requiredUserGroups = requiredUserGroups;
        orUserGroups = new ArrayList<>();
    }

    public SearchByUserGroupsCriteria(Set<String> requiredUserGroups, List<Set<String>> orUserGroups) {
        this.requiredUserGroups = requiredUserGroups;
        this.orUserGroups = orUserGroups;
    }

    public void addToRequiredUserGroups(String role) {
        requiredUserGroups.add(role);
    }

    public void removeFromRequiredUserGroups(String role) {
        requiredUserGroups.remove(role);
    }

    public boolean containsRequiredUserGroup(String role) {
        return requiredUserGroups.contains(role);
    }

    public Set<String> getRequiredUserGroups() {
        return requiredUserGroups;
    }

    public void setRequiredUserGroups(Set<String> requiredUserGroups) {
        this.requiredUserGroups = requiredUserGroups;
    }

    public void addToOrUserGroups(Set<String> roles) {
        orUserGroups.add(roles);
    }

    public void removeFromOrUserGroups(Set<String> roles) {
        orUserGroups.remove(roles);
    }

    public List<Set<String>> getOrUserGroups() {
        return orUserGroups;
    }

    public void setOrUserGroups(List<Set<String>> orUserGroups) {
        this.orUserGroups = orUserGroups;
    }
}

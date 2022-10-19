/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.valtimo.keycloak.security.jwt.service;

import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria;
import com.valtimo.keycloak.service.KeycloakService;
import com.valtimo.keycloak.service.KeycloakUserManagementService;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakUserManagementServiceTest {

    private KeycloakService keycloakService;
    private KeycloakUserManagementService userManagementService;

    private UserRepresentation jamesVance;
    private UserRepresentation johnDoe;
    private UserRepresentation ashaMiller;

    @BeforeEach
    public void before() {
        keycloakService = mock(KeycloakService.class, RETURNS_DEEP_STUBS);
        userManagementService = new KeycloakUserManagementService(keycloakService, "clientName");

        jamesVance = newUser("James", "Vance", List.of(USER));
        johnDoe = newUser("John", "Doe", List.of(USER, ADMIN));
        ashaMiller = newUser("Asha", "Miller", List.of(ADMIN));

        when(keycloakService.realmRolesResource().get(USER).getRoleUserMembers())
            .thenReturn(Set.of(johnDoe, jamesVance));
        when(keycloakService.realmRolesResource().get(ADMIN).getRoleUserMembers())
            .thenReturn(Set.of(johnDoe, ashaMiller));
    }

    @Test
    void shouldFindMultipleUsersForRequiredUserGroup() {
        var search = new SearchByUserGroupsCriteria();
        search.addToRequiredUserGroups(USER);

        var users = userManagementService.findByRoles(search);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsOnly(jamesVance.getId(), johnDoe.getId());
    }

    @Test
    void shouldFindUsersForMultipleRequiredUserGroup() {
        var search = new SearchByUserGroupsCriteria();
        search.addToRequiredUserGroups(USER);
        search.addToRequiredUserGroups(ADMIN);

        var users = userManagementService.findByRoles(search);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsOnlyOnce(johnDoe.getId());
    }

    @Test
    void shouldDoOrUserGroups() {
        var search = new SearchByUserGroupsCriteria();
        search.addToOrUserGroups(Set.of(USER, ADMIN));

        var users = userManagementService.findByRoles(search);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsOnlyOnce(jamesVance.getId(), johnDoe.getId(), ashaMiller.getId());
    }

    @Test
    void shouldDoAndUserGroups() {
        var search = new SearchByUserGroupsCriteria();
        search.addToOrUserGroups(Set.of(USER));
        search.addToOrUserGroups(Set.of(ADMIN));

        var users = userManagementService.findByRoles(search);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsOnlyOnce(johnDoe.getId());
    }

    @Test
    void findByRoleShouldReturnEmptyListWhenNotFoundExceptionIsThrown() {
        when( keycloakService.realmRolesResource().get("some-role").getRoleUserMembers())
            .thenThrow(new NotFoundException());

        var users = userManagementService.findByRole("some-role");

        assertThat(users).isEmpty();
    }

    private UserRepresentation newUser(String firstName, String lastName, List<String> roles) {
        var user = new UserRepresentation();
        user.setId(Integer.toString(Objects.hash(firstName, lastName, roles)));
        user.setEnabled(true);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        var roleRepresentations = roles.stream()
            .map(role -> new RoleRepresentation(role, role + " description", false))
            .collect(Collectors.toList());
        when(keycloakService.usersResource().get(user.getId()).roles().realmLevel().listAll())
            .thenReturn(roleRepresentations);
        return user;
    }
}

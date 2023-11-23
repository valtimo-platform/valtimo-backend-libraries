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

package com.valtimo.keycloak.service;

import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.DEVELOPER;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static com.valtimo.keycloak.service.KeycloakUserManagementService.MAX_USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

        when(keycloakService.realmRolesResource(any()).get(USER).getRoleUserMembers(0, MAX_USERS))
            .thenReturn(Set.of(johnDoe, jamesVance));
        when(keycloakService.realmRolesResource(any()).get(ADMIN).getRoleUserMembers(0, MAX_USERS))
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
        assertThat(userIds).containsExactly(ashaMiller.getId(), jamesVance.getId(), johnDoe.getId());
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
    void shouldFindUsersWithClientRoles() {
        var markUser = new UserRepresentation();
        markUser.setId("developer-john-id");
        markUser.setEnabled(true);
        markUser.setFirstName("Mark");
        markUser.setLastName("Smit");
        var roleRepresentation = new RoleRepresentation(DEVELOPER, "developer", false);
        when(keycloakService.usersResource(any()).get(markUser.getId()).roles().realmLevel().listAll())
            .thenReturn(List.of());
        when(keycloakService.usersResource(any()).get(markUser.getId()).roles().clientLevel(any()).listAll())
            .thenReturn(List.of(roleRepresentation));
        when(keycloakService.clientRolesResource(any()).get(DEVELOPER).getRoleUserMembers(0, MAX_USERS))
            .thenReturn(Set.of(markUser));
        var search = new SearchByUserGroupsCriteria();
        search.addToOrUserGroups(Set.of(DEVELOPER));

        var users = userManagementService.findByRoles(search);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsExactly(markUser.getId());
    }

    @Test
    void findByRoleShouldReturnEmptyListWhenNotFoundExceptionIsThrown() {
        when( keycloakService.realmRolesResource(any()).get("some-role").getRoleUserMembers())
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
        when(keycloakService.usersResource(any()).get(user.getId()).roles().realmLevel().listAll())
            .thenReturn(roleRepresentations);
        when(keycloakService.usersResource(any()).get(user.getId()).roles().clientLevel(any()).listAll())
            .thenReturn(List.of());
        return user;
    }
}

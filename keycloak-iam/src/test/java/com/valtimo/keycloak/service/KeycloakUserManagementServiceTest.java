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

package com.valtimo.keycloak.service;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.DEVELOPER;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static com.valtimo.keycloak.service.KeycloakUserManagementService.MAX_USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ritense.valtimo.contract.OauthConfigHolder;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.config.ValtimoProperties.Oauth;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class KeycloakUserManagementServiceTest {

    private KeycloakService keycloakService;
    private KeycloakUserManagementService userManagementService;
    private CacheManager cacheManager;
    private CacheManagerUserCache cacheManagerUserCache;

    private UserRepresentation jamesVance;
    private UserRepresentation johnDoe;
    private UserRepresentation ashaMiller;

    @BeforeAll
    static void setUp() {
        new OauthConfigHolder(new Oauth());
    }

    @BeforeEach
    public void before() {
        keycloakService = mock(KeycloakService.class, RETURNS_DEEP_STUBS);
        cacheManager = new ConcurrentMapCacheManager();
        cacheManagerUserCache = new CacheManagerUserCache(cacheManager);
        userManagementService = new KeycloakUserManagementService(keycloakService, "clientName", cacheManagerUserCache);

        jamesVance = newUser("James", "Vance", List.of(USER));
        johnDoe = newUser("John", "Doe", List.of(USER, ADMIN));
        ashaMiller = newUser("Asha", "Miller", List.of(ADMIN));

        when(keycloakService.realmRolesResource(any()).get(USER).getUserMembers(0, MAX_USERS))
            .thenReturn(List.of(johnDoe, jamesVance));
        when(keycloakService.realmRolesResource(any()).get(ADMIN).getUserMembers(0, MAX_USERS))
            .thenReturn(List.of(johnDoe, ashaMiller));
        when(keycloakService.clientRolesResource(any()).get(any()).getUserMembers(0, MAX_USERS))
            .thenReturn(List.of());
        when(keycloakService.realmRolesResource(any()).get(any()).getRoleGroupMembers())
            .thenReturn(Set.of());
        when(keycloakService.clientRolesResource(any()).get(any()).getRoleGroupMembers())
            .thenReturn(Set.of());
    }

    @AfterEach
    public void after() {
        reset(keycloakService);
    }

    @Test
    void shouldFindMultipleUsersForRequiredUserGroup() {
        var search = new SearchByUserGroupsCriteria();
        search.addToRequiredUserGroups(USER);

        var users = userManagementService.findByRoles(search);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsOnlyOnce(jamesVance.getId(), johnDoe.getId());
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
        when(keycloakService.usersResource(any()).get(markUser.getId()).roles().realmLevel().listEffective(true))
            .thenReturn(List.of());
        when(keycloakService.usersResource(any()).get(markUser.getId()).roles().clientLevel(any()).listEffective(true))
            .thenReturn(List.of(roleRepresentation));
        when(keycloakService.realmRolesResource(any()).get(DEVELOPER).getUserMembers(0, MAX_USERS))
            .thenReturn(List.of());
        when(keycloakService.clientRolesResource(any()).get(DEVELOPER).getUserMembers(0, MAX_USERS))
            .thenReturn(List.of(markUser));
        var search = new SearchByUserGroupsCriteria();
        search.addToOrUserGroups(Set.of(DEVELOPER));

        var users = userManagementService.findByRoles(search);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsExactly(markUser.getId());
    }

    @Test
    void findByRoleShouldReturnEmptyListWhenExceptionIsThrown() {
        when(keycloakService.realmRolesResource(any()).get("some-role").getUserMembers(0, MAX_USERS))
            .thenThrow(new NotFoundException());

        var users = userManagementService.findByRole("some-role");

        assertThat(users).isEmpty();
    }

    @Test
    void shouldDeDuplicateUsersForRequiredUserGroup() {
        var duplicateJames = newUser(jamesVance.getFirstName(), jamesVance.getLastName(), List.of(USER));
        duplicateJames.setId(jamesVance.getId());

        when(keycloakService.clientRolesResource(any()).get(USER).getUserMembers(0, MAX_USERS))
            .thenReturn(List.of(duplicateJames));

        var users = userManagementService.findByRole(USER);

        var userIds = users.stream().map(ManageableUser::getId).collect(Collectors.toList());
        assertThat(userIds).containsOnlyOnce(jamesVance.getId(), johnDoe.getId());
    }

    @Test
    void findByUserIdentifierShouldReturnUserWhenSearchingOnUserId() {
        OauthConfigHolder.getCurrentInstance().setIdentifierField(ValtimoProperties.IdentifierField.USERID);

        when(keycloakService.usersResource(any()).get(eq(johnDoe.getId())).toRepresentation())
            .thenReturn(johnDoe);

        var user = userManagementService.findByUserIdentifier(johnDoe.getId());

        verify(keycloakService.usersResource(any()).get(eq(johnDoe.getId()))).toRepresentation();
        assertThat(user).isNotNull();
    }

    @Test
    void findByUserIdentifierShouldReturnUserWhenSearchingOnUsername() {
        OauthConfigHolder.getCurrentInstance().setIdentifierField(ValtimoProperties.IdentifierField.USERNAME);

        when(keycloakService.usersResource(any()).search(eq(johnDoe.getUsername())))
            .thenReturn(List.of(johnDoe));

        var user = userManagementService.findByUserIdentifier(johnDoe.getUsername());

        verify(keycloakService.usersResource(any())).search(eq(johnDoe.getUsername()));
        assertThat(user).isNotNull();
    }

    @Test
    void shouldRetrieveManageableUserFromCache() {
        String email = "test@example.com";

        userManagementService.findByEmail(email);
        userManagementService.findByEmail(email);

        verify(keycloakService.usersResource(any()), times(1)).search(null, null, null, email, 0, 1, true, true);
    }

    @Test
    void findByUserIdentifierShouldNotThrowAnExceptionWhenSearchingOnUsernameAndNoUserIsNotFound() {
        OauthConfigHolder.getCurrentInstance().setIdentifierField(ValtimoProperties.IdentifierField.USERNAME);

        when(keycloakService.usersResource(any()).search(eq(johnDoe.getUsername())))
            .thenReturn(List.of());

        var user = userManagementService.findByUserIdentifier(johnDoe.getUsername());

        verify(keycloakService.usersResource(any())).search(eq(johnDoe.getUsername()));
        assertThat(user).isNull();
    }

    private UserRepresentation newUser(String firstName, String lastName, List<String> roles, String username) {
        var user = new UserRepresentation();
        user.setId(Integer.toString(Objects.hash(firstName, lastName, roles)));
        user.setEnabled(true);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        var roleRepresentations = roles.stream()
            .map(role -> new RoleRepresentation(role, role + " description", false))
            .collect(Collectors.toList());
        when(keycloakService.usersResource(any()).get(user.getId()).roles().realmLevel().listEffective(true))
            .thenReturn(roleRepresentations);
        when(keycloakService.usersResource(any()).get(user.getId()).roles().clientLevel(any()).listEffective(true))
            .thenReturn(List.of());
        return user;
    }

    private UserRepresentation newUser(String firstName, String lastName, List<String> roles) {
        return newUser(firstName, lastName, roles, "username");
    }
}

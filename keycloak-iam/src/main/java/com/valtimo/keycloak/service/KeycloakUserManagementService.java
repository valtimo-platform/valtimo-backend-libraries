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

import static com.ritense.valtimo.contract.Constants.SYSTEM_ACCOUNT;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import com.ritense.valtimo.contract.OauthConfigHolder;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.UserNotFoundException;
import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

public class KeycloakUserManagementService implements UserManagementService {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserManagementService.class);
    protected static final int MAX_USERS = 1000;
    private static final String MAX_USERS_WARNING_MESSAGE = "Maximum number of users retrieved from keycloak: " + MAX_USERS + ".";
    private static final ValtimoUser SYSTEM_VALTIMO_USER = new ValtimoUserBuilder().id(SYSTEM_ACCOUNT).lastName(SYSTEM_ACCOUNT).build();

    private final KeycloakService keycloakService;
    private final String clientName;

    public KeycloakUserManagementService(KeycloakService keycloakService, String keycloakClientName) {
        this.keycloakService = keycloakService;
        this.clientName = keycloakClientName;
    }

    @Override
    public ManageableUser createUser(ManageableUser user) {
        throw new NotImplementedException();
    }

    @Override
    public ManageableUser updateUser(ManageableUser updatedUserData) throws UserNotFoundException {
        throw new NotImplementedException();
    }

    @Override
    public void deleteUser(String userId) {
        throw new NotImplementedException();
    }

    @Override
    public boolean resendVerificationEmail(String userId) {
        throw new NotImplementedException();
    }

    @Override
    public void activateUser(String userId) {
        throw new NotImplementedException();
    }

    @Override
    public void deactivateUser(String userId) {
        throw new NotImplementedException();
    }

    public Integer countUsers() {
        try (Keycloak keycloak = keycloakService.keycloak()) {
            return keycloakService.usersResource(keycloak).count();
        }
    }

    @Override
    public List<ManageableUser> getAllUsers() {
        List<ManageableUser> users;
        try (Keycloak keycloak = keycloakService.keycloak()) {
            users = keycloakService.usersResource(keycloak).list(0, MAX_USERS).stream()
                .filter(UserRepresentation::isEnabled)
                .map(this::toManageableUserByRetrievingRoles)
                .toList();
        }

        if (users.size() >= MAX_USERS) {
            logger.warn(MAX_USERS_WARNING_MESSAGE);
        }

        return users;
    }

    @Override
    public Page<ManageableUser> getAllUsers(Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public Page<ManageableUser> queryUsers(String searchTerm, Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<ManageableUser> findByEmail(String email) {
        return findUserRepresentationByEmail(email).map(this::toManageableUserByRetrievingRoles);
    }

    @Override
    public Optional<NamedUser> findNamedUserByEmail(String email) {
        return findUserRepresentationByEmail(email).map(this::toNamedUser);
    }

    @Override
    public ValtimoUser findByUserIdentifier(String userIdentifier) {
        UserRepresentation user = null;
        try (Keycloak keycloak = keycloakService.keycloak()) {
            switch (OauthConfigHolder.getCurrentInstance().getIdentifierField()) {
                case USERID ->
                    user = keycloakService.usersResource(keycloak).get(userIdentifier).toRepresentation();
                case USERNAME -> {
                    var users = keycloakService.usersResource(keycloak).search(userIdentifier);
                    if (!users.isEmpty()) {
                        user = users.get(0);
                    }
                }
            }
        }
        Boolean isUserEnabled = user != null ? user.isEnabled() : null;
        return Boolean.TRUE.equals(isUserEnabled) ? toValtimoUserByRetrievingRoles(user) : null;
    }

    @Override
    public ValtimoUser findById(String userId) {
        UserRepresentation user;
        if (userId.equals(SYSTEM_ACCOUNT)) {
            return SYSTEM_VALTIMO_USER;
        } else {
            try (Keycloak keycloak = keycloakService.keycloak()) {
                user = keycloakService.usersResource(keycloak).get(userId).toRepresentation();
            }
            return Boolean.TRUE.equals(user.isEnabled()) ? toValtimoUserByRetrievingRoles(user) : null;
        }
    }

    @Override
    public List<ManageableUser> findByRole(String authority) {
        return findUserRepresentationByRole(authority).stream()
            .map(this::toManageableUserByRetrievingRoles)
            .toList();
    }

    @Override
    public List<ManageableUser> findByRoles(SearchByUserGroupsCriteria groupsCriteria) {
        Set<String> allUserGroups = new HashSet<>(groupsCriteria.getRequiredUserGroups());
        groupsCriteria.getOrUserGroups().forEach(allUserGroups::addAll);

        List<ManageableUser> allUsers = allUserGroups.stream()
            .map(this::findByRole)
            .flatMap(Collection::stream)
            .distinct()
            .toList();

        return allUsers.stream()
            .filter(user -> new HashSet<>(user.getRoles()).containsAll(groupsCriteria.getRequiredUserGroups()))
            .filter(user -> groupsCriteria.getOrUserGroups().stream()
                .map(userGroups -> user.getRoles().stream().anyMatch(userGroups::contains))
                .reduce(true, (orUserGroup1, orUserGroup2) -> orUserGroup1 && orUserGroup2))
            .sorted(comparing(ManageableUser::getFullName, nullsLast(naturalOrder())))
            .toList();
    }

    @Override
    public List<NamedUser> findNamedUserByRoles(Set<String> roles) {
        return roles.stream()
            .map(this::findUserRepresentationByRole)
            .flatMap(Collection::stream)
            .map(this::toNamedUser)
            .distinct()
            .sorted(comparing(NamedUser::getLabel))
            .toList();
    }

    @Override
    public ManageableUser getCurrentUser() {
        if (SecurityUtils.getCurrentUserAuthentication() == null) {
            return SYSTEM_VALTIMO_USER;
        } else if (SecurityUtils.getCurrentUserAuthentication() instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            return findByEmail(SecurityUtils.getCurrentUserLogin()).orElseThrow(() ->
                new IllegalStateException("No user found for email: ${currentUserService.currentUser.email}")
            );
        }
    }

    @Override
    public String getCurrentUserId() {
        if (SecurityUtils.getCurrentUserAuthentication() != null) {
            return findUserRepresentationByEmail(SecurityUtils.getCurrentUserLogin()).orElseThrow(() ->
                new IllegalStateException("No user found for email: " + SecurityUtils.getCurrentUserLogin())
            ).getId();
        } else {
            return SYSTEM_ACCOUNT;
        }
    }

    private Optional<UserRepresentation> findUserRepresentationByEmail(String email) {
        if (email == null || !email.contains("@")) {
            return Optional.empty();
        }
        List<UserRepresentation> userList;
        try (Keycloak keycloak = keycloakService.keycloak()) {
            userList = keycloakService
                .usersResource(keycloak)
                .search(null, null, null, email, 0, 1, true, true);
        }
        if (userList.isEmpty() || !Objects.equals(userList.get(0).getEmail(), email)) {
            return Optional.empty();
        } else {
            return Optional.of(userList.get(0));
        }
    }

    private List<UserRepresentation> findUserRepresentationByRole(String authority) {

        List<List<UserRepresentation>> usersList = new ArrayList<>();
        try (Keycloak keycloak = keycloakService.keycloak()) {
            Set<GroupRepresentation> roleGroups = new HashSet<>();
            try {
                RoleResource roleResource = keycloakService.realmRolesResource(keycloak).get(authority);
                usersList.add(roleResource.getUserMembers(0, MAX_USERS));
                roleGroups.addAll(roleResource.getRoleGroupMembers());
            } catch (NotFoundException e) {
                logger.debug("Failed to find users by realm. Error: {}", e.getMessage());
            }
            if (!clientName.isBlank()) {
                try {
                    RoleResource roleResource = keycloakService.clientRolesResource(keycloak).get(authority);
                    usersList.add(roleResource.getUserMembers(0, MAX_USERS));
                    roleGroups.addAll(roleResource.getRoleGroupMembers());
                } catch (NotFoundException e) {
                    logger.debug("Failed to find users by client. Error: {}", e.getMessage());
                }
            }
            try {
                for (GroupRepresentation group : roleGroups) {
                    usersList.add(keycloakService.realmResource(keycloak)
                        .groups()
                        .group(group.getId())
                        .members(0, MAX_USERS));
                }
            } catch (NotFoundException e) {
                logger.debug("Failed to find users by group. Error: {}", e.getMessage());
            }
        }

        usersList.forEach(users -> {
            if (users.size() >= MAX_USERS) {
                logger.warn(MAX_USERS_WARNING_MESSAGE);
            }
        });

        var users = usersList.stream()
            .flatMap(Collection::stream)
            .filter(UserRepresentation::isEnabled)
            .map(UserRepresentationWrapper::new)
            .distinct()
            .map(UserRepresentationWrapper::userRepresentation)
            .toList();

        if (users.isEmpty()) {
            logger.error("No active users found with role {}", authority);
        }

        return users;
    }

    private ManageableUser toManageableUserByRetrievingRoles(UserRepresentation userRepresentation) {
        return new ValtimoUserBuilder()
            .id(userRepresentation.getId())
            .username(userRepresentation.getUsername())
            .firstName(userRepresentation.getFirstName())
            .lastName(userRepresentation.getLastName())
            .email(userRepresentation.getEmail())
            .roles(getRolesAsStringFromUser(userRepresentation)) // <- does an additional call to retrieve the roles
            .build();
    }

    private NamedUser toNamedUser(UserRepresentation userRepresentation) {
        return new NamedUser(
            userRepresentation.getId(),
            userRepresentation.getEmail(),
            userRepresentation.getFirstName(),
            userRepresentation.getLastName(),
            userRepresentation.getUsername()
        );
    }

    private List<String> getRolesAsStringFromUser(UserRepresentation userRepresentation) {
        return getRolesFromUser(userRepresentation)
            .stream()
            .map(RoleRepresentation::getName)
            .toList();
    }

    private List<RoleRepresentation> getRolesFromUser(UserRepresentation userRepresentation) {
        try (Keycloak keycloak = keycloakService.keycloak()) {
            var realmRoles = keycloakService
                .usersResource(keycloak)
                .get(userRepresentation.getId())
                .roles().realmLevel().listEffective(true);
            var roles = new ArrayList<>(realmRoles);
            if (!clientName.isBlank()) {
                var clientRoles = keycloakService
                    .usersResource(keycloak)
                    .get(userRepresentation.getId())
                    .roles().clientLevel(keycloakService.getClientId(keycloak)).listEffective(true);
                roles.addAll(clientRoles);
            }
            return roles;
        }
    }

    private ValtimoUser toValtimoUserByRetrievingRoles(UserRepresentation userRepresentation) {
        return (ValtimoUser) toManageableUserByRetrievingRoles(userRepresentation);
    }

    private record UserRepresentationWrapper(UserRepresentation userRepresentation) {

        String getId() {
            return userRepresentation.getId();
        }

        String getUsername() {
            return userRepresentation.getUsername();
        }

        String getEmail() {
            return userRepresentation.getEmail();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UserRepresentationWrapper that = (UserRepresentationWrapper) o;
            return Objects.equals(getId(), that.getId())
                && Objects.equals(getUsername(), that.getUsername())
                && Objects.equals(getEmail(), that.getEmail());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getUsername(), getEmail());
        }
    }
}

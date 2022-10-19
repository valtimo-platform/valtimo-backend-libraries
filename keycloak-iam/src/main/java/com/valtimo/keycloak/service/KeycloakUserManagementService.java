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

package com.valtimo.keycloak.service;

import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.UserNotFoundException;
import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.Constants.SYSTEM_ACCOUNT;

public class KeycloakUserManagementService implements UserManagementService {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserManagementService.class);

    private final KeycloakService keycloakService;
    private final String clientName;

    public KeycloakUserManagementService(KeycloakService keycloakService, String keycloakClientName) {
        this.keycloakService = keycloakService;
        this.clientName = keycloakClientName;
    }

    @Override
    public ManageableUser createUser(ManageableUser user) {
        return null;
    }

    @Override
    public ManageableUser updateUser(ManageableUser updatedUserData) throws UserNotFoundException {
        return null;
    }

    @Override
    public void deleteUser(String userId) {
    }

    @Override
    public boolean resendVerificationEmail(String userId) {
        return false;
    }

    @Override
    public void activateUser(String userId) {
    }

    @Override
    public void deactivateUser(String userId) {
    }

    @Override
    public Page<ManageableUser> getAllUsers(Pageable pageable) {
        return null;
    }

    public Integer countUsers() {
        return keycloakService.usersResource().count();
    }

    @Override
    public List<ManageableUser> getAllUsers() {
        return keycloakService.usersResource().list().stream()
            .filter(UserRepresentation::isEnabled)
            .map(this::userRepresentationToManagableUser)
            .collect(Collectors.toList());
    }

    @Override
    public Page<ManageableUser> queryUsers(String searchTerm, Pageable pageable) {
        return null;
    }

    @Override
    public Optional<ManageableUser> findByEmail(String email) {
        var userList = keycloakService
            .usersResource()
            .search(null, null, null, email, 0, 1, true, true);
        return userList.isEmpty() ? Optional.empty() : Optional.of(userRepresentationToManagableUser(userList.get(0)));
    }

    @Override
    public ValtimoUser findById(String userId) {
        var user = keycloakService.usersResource().get(userId).toRepresentation();
        return user.isEnabled() ? userRepresentationToValtimoUser(user) : null;
    }

    @Override
    public List<ManageableUser> findByRole(String authority) {
        Set<UserRepresentation> roleUserMembers = new HashSet<>();
        boolean rolesFound = false;

        try {
            roleUserMembers.addAll(keycloakService.realmRolesResource().get(authority).getRoleUserMembers());
            rolesFound = true;
        } catch (NotFoundException e) {
            logger.debug("Could not find realm roles: {}", e.getMessage());
        }

        if (!clientName.isBlank()) {
            try {
                roleUserMembers.addAll(keycloakService.clientRolesResource().get(authority).getRoleUserMembers());
                rolesFound = true;
            } catch (NotFoundException e) {
                logger.debug("Could not find client roles: {}", e.getMessage());
            }
        }

        if (!rolesFound) {
            throw new NotFoundException("Role not Found: "+authority);
        }

        return roleUserMembers.stream()
            .filter(UserRepresentation::isEnabled)
            .map(this::userRepresentationToManagableUser)
            .collect(Collectors.toList());
    }

    @Override
    public List<ManageableUser> findByRoles(SearchByUserGroupsCriteria groupsCriteria) {
        Set<String> allUserGroups = new HashSet<>(groupsCriteria.getRequiredUserGroups());
        groupsCriteria.getOrUserGroups().forEach(allUserGroups::addAll);

        List<ManageableUser> allUsers = allUserGroups.stream()
            .map(this::findByRole)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());

        return allUsers.stream()
            .filter(user -> user.getRoles().containsAll(groupsCriteria.getRequiredUserGroups()))
            .filter(user -> groupsCriteria.getOrUserGroups().stream()
                .map(userGroups -> user.getRoles().stream().anyMatch(userGroups::contains))
                .reduce(true, (orUserGroup1, orUserGroup2) -> orUserGroup1 && orUserGroup2))
            .collect(Collectors.toList());
    }

    @Override
    public ManageableUser getCurrentUser() {
        if (SecurityUtils.getCurrentUserAuthentication() != null) {
            return findByEmail(SecurityUtils.getCurrentUserLogin()).orElseThrow(() ->
                new IllegalStateException("No user found for email: ${currentUserService.currentUser.email}")
            );
        } else {
            return new ValtimoUserBuilder().id(SYSTEM_ACCOUNT).lastName(SYSTEM_ACCOUNT).build();
        }
    }

    private ManageableUser userRepresentationToManagableUser(UserRepresentation userRepresentation) {
        return new ValtimoUserBuilder()
            .id(userRepresentation.getId())
            .username(userRepresentation.getUsername())
            .firstName(userRepresentation.getFirstName())
            .lastName(userRepresentation.getLastName())
            .email(userRepresentation.getEmail())
            .roles(getRolesAsStringFromUser(userRepresentation))
            .build();
    }

    private List<String> getRolesAsStringFromUser(UserRepresentation userRepresentation) {
        return getRolesFromUser(userRepresentation)
            .stream()
            .map(RoleRepresentation::getName)
            .collect(Collectors.toList());
    }

    private List<RoleRepresentation> getRolesFromUser(UserRepresentation userRepresentation) {
        return keycloakService
            .usersResource()
            .get(userRepresentation.getId())
            .roles().realmLevel().listAll();
    }

    private ValtimoUser userRepresentationToValtimoUser(UserRepresentation userRepresentation) {
        return (ValtimoUser) userRepresentationToManagableUser(userRepresentation);
    }
}

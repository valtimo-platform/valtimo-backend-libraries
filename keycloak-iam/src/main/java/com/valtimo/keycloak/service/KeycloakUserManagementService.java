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
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class KeycloakUserManagementService implements UserManagementService {

    private final KeycloakService keycloakService;

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
        return keycloakService.rolesResource().get(authority).getRoleUserMembers().stream()
            .filter(UserRepresentation::isEnabled)
            .map(this::userRepresentationToManagableUser)
            .collect(Collectors.toList());
    }

    @Override
    public List<ManageableUser> findByRoles(SearchByUserGroupsCriteria groupsCriteria) {
        return null;
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
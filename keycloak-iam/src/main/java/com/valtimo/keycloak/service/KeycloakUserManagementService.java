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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        List<ManageableUser> users = keycloakService.usersResource().list().stream().map(
            user -> new ValtimoUserBuilder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRealmRoles()).build()
        ).collect(Collectors.toList());

        return new PageImpl<>(users);
    }

    @Override
    public Page<ManageableUser> queryUsers(String searchTerm, Pageable pageable) {
        return null;
    }

    @Override
    public Optional<ManageableUser> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public ValtimoUser findById(String userId) {
        return null;
    }

    @Override
    public List<ManageableUser> findByRole(String authority) {
        return keycloakService.rolesResource().get(authority).getRoleUserMembers().stream().map(
            user -> new ValtimoUserBuilder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRealmRoles()).build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<ManageableUser> findByRoles(SearchByUserGroupsCriteria groupsCriteria) {
        return null;
    }

}
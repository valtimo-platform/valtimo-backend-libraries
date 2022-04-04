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

package com.ritense.valtimo.contract.authentication;

import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserManagementService {

    ManageableUser createUser(ManageableUser user);

    ManageableUser updateUser(ManageableUser user) throws UserNotFoundException;

    void deleteUser(String userId);

    boolean resendVerificationEmail(String userId);

    void activateUser(String userId);

    void deactivateUser(String userId);

    Page<ManageableUser> getAllUsers(Pageable pageable);

    List<ManageableUser> getAllUsers();

    Page<ManageableUser> queryUsers(String searchTerm, Pageable pageable);

    Optional<ManageableUser> findByEmail(String email);

    ManageableUser findById(String userId);

    List<ManageableUser> findByRole(String authority);

    List<ManageableUser> findByRoles(SearchByUserGroupsCriteria groupsCriteria);
}
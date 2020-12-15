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

package com.ritense.valtimo.service;

import com.ritense.valtimo.contract.authentication.AuthorizedUserRepository;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.springframework.security.core.Authentication;

import java.util.Collection;

public class AuthorizedUsersService implements com.ritense.valtimo.contract.authentication.AuthorizedUsersService {

    private final Collection<AuthorizedUserRepository> authorizedUserRepositories;

    public AuthorizedUsersService(Collection<AuthorizedUserRepository> authorizedUserRepositories) {
        this.authorizedUserRepositories = authorizedUserRepositories;
    }

    private AuthorizedUserRepository findAuthorizedUserRepository() throws IllegalAccessException, IllegalStateException {

        Authentication authentication = SecurityUtils.getCurrentUserAuthentication();
        if (authentication == null) {
            throw new IllegalAccessException("No authentication");
        }

        if (SecurityUtils.isAuthenticated()) {
            String currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin == null) {
                throw new IllegalAccessException("No current user logged in.");
            }
        }

        //Find implementation
        for (AuthorizedUserRepository authorizedUserRepository : authorizedUserRepositories) {
            if (authorizedUserRepository.supports(authentication.getClass())) {
                return authorizedUserRepository;
            }
        }
        throw new IllegalStateException("No authorized user repository implementation found supporting : " + authentication.getClass());
    }

    @Override
    public boolean isRoleInUse(String name) throws IllegalAccessException {
        AuthorizedUserRepository authorizedUserRepository = findAuthorizedUserRepository();
        return authorizedUserRepository.isRoleInUse(name);
    }

}

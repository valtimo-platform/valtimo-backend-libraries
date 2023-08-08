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

package com.ritense.valtimo.service;

import com.ritense.valtimo.contract.authentication.CurrentUserRepository;
import com.ritense.valtimo.contract.authentication.CurrentUserService;
import com.ritense.valtimo.contract.authentication.model.Profile;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import java.util.Collection;

public class CurrentUserServiceImpl implements CurrentUserService {

    private final Collection<CurrentUserRepository> currentUserRepositories;

    public CurrentUserServiceImpl(Collection<CurrentUserRepository> currentUserRepositories) {
        this.currentUserRepositories = currentUserRepositories;
    }

    private CurrentUserRepository findCurrentUserRepository() throws IllegalAccessException, IllegalStateException {

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
        for (CurrentUserRepository currentUserRepository : currentUserRepositories) {
            if (currentUserRepository.supports(authentication.getClass())) {
                return currentUserRepository;
            }
        }
        throw new IllegalStateException("No current user repository implementation found supporting : " + authentication.getClass());
    }

    public ValtimoUser getCurrentUser() throws IllegalAccessException {
        CurrentUserRepository currentUserRepository = findCurrentUserRepository();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        return currentUserRepository.getCurrentUser(currentUserLogin);
    }

    @Override
    public void changePassword(String newPassword) throws IllegalStateException, IllegalAccessException {
        CurrentUserRepository currentUserRepository = findCurrentUserRepository();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        currentUserRepository.changePassword(currentUserLogin, newPassword);
    }

    @Override
    public void updateProfile(Profile profile) throws IllegalAccessException {
        CurrentUserRepository currentUserRepository = findCurrentUserRepository();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        currentUserRepository.updateProfile(currentUserLogin, profile);
    }

}
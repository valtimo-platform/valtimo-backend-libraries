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

package com.valtimo.keycloak.repository;

import static com.ritense.valtimo.contract.utils.SecurityUtils.getCurrentUserRoles;

import com.ritense.valtimo.contract.authentication.CurrentUserRepository;
import com.ritense.valtimo.contract.authentication.model.Profile;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class KeycloakCurrentUserRepository implements CurrentUserRepository {

    @Override
    public ValtimoUser getCurrentUser(String currentUserLogin) {

        //Discuss the option to get data via API
        ValtimoUserBuilder valtimoUserBuilder = new ValtimoUserBuilder()
            .id(currentUserLogin)
            .username(currentUserLogin)
            .email(currentUserLogin)
            .roles(getCurrentUserRoles());
        return valtimoUserBuilder.build();
    }

    @Override
    public void changePassword(String currentUserLogin, String newPassword) {
        throw new UnsupportedOperationException("changePassword via keycloak");
    }

    @Override
    public void updateProfile(String currentUserLogin, Profile profile) {
        throw new UnsupportedOperationException("updateProfile via keycloak");
    }

    @Override
    public boolean supports(Class<? extends Authentication> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
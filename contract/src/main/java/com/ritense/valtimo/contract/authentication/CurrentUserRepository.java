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

import com.ritense.valtimo.contract.authentication.model.Profile;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import org.springframework.security.core.Authentication;

public interface CurrentUserRepository {

    ValtimoUser getCurrentUser(String currentUserLogin);

    void changePassword(String currentUserLogin, String newPassword);

    void updateProfile(String currentUserLogin, Profile profile);

    boolean supports(Class<? extends Authentication> authentication);

}
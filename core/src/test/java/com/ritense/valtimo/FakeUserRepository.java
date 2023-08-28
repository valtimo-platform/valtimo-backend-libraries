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

package com.ritense.valtimo;

import com.ritense.valtimo.contract.authentication.CurrentUserRepository;
import com.ritense.valtimo.contract.authentication.model.Profile;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import java.util.List;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;

public class FakeUserRepository implements CurrentUserRepository {

    @Override
    public ValtimoUser getCurrentUser(String currentUserLogin) {
        var builder = new ValtimoUserBuilder()
            .id("anId")
            .username("aUserName")
            .name("aName")
            .email(currentUserLogin)
            .firstName("aFirstName")
            .lastName("aLastName")
            .phoneNo("aPhoneNumber")
            .isEmailVerified(true)
            .langKey("NL")
            .blocked(false)
            .activated(true)
            .roles(List.of(USER));
        return builder.build();
    }

    @Override
    public void changePassword(String currentUserLogin, String newPassword) {

    }

    @Override
    public void updateProfile(String currentUserLogin, Profile profile) {

    }

    @Override
    public boolean supports(Class<? extends Authentication> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
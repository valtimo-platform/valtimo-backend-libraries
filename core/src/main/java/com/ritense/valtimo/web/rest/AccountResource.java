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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.contract.authentication.CurrentUserService;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.TEXT_PLAIN_UTF8_VALUE;

@ConditionalOnBean(UserManagementService.class)
@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class AccountResource {

    private final CurrentUserService currentUserService;

    public AccountResource(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/v1/account")
    public ResponseEntity<ManageableUser> getAccount() throws IllegalAccessException {
        final ManageableUser currentUser = currentUserService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/v1/account/profile")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody Profile profile) throws IllegalAccessException {
        currentUserService.updateProfile(profile);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/v1/account/change_password", produces = TEXT_PLAIN_UTF8_VALUE)
    public ResponseEntity<Void> changePassword(@RequestBody String password) throws IllegalAccessException {
        currentUserService.changePassword(password);
        return ResponseEntity.ok().build();
    }

}

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

import camundajar.impl.com.google.gson.JsonElement;
import camundajar.impl.com.google.gson.JsonParser;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.service.UserSettingsService;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class UserResource {

    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
    private final UserManagementService userManagementService;
    private final UserSettingsService userSettingsService;

    public UserResource(UserManagementService userManagementService, UserSettingsService userSettingsService) {
        this.userManagementService = userManagementService;
        this.userSettingsService = userSettingsService;
    }

    @PostMapping("/v1/users")
    public ResponseEntity<ManageableUser> createUser(@RequestBody ValtimoUser valtimoUser) throws URISyntaxException {
        logger.debug("Request to save ValtimoUser : {}", valtimoUser);
        final ManageableUser user = userManagementService.createUser(valtimoUser);
        final URI uri = new URI("/api/v1/users/" + UriUtils.encode(user.getId(), StandardCharsets.UTF_8));
        final HttpHeaders headers = HeaderUtil.createAlert("userManagement.created", user.getEmail());
        return ResponseEntity.created(uri).headers(headers).body(user);
    }

    @PutMapping("/v1/users")
    public ResponseEntity<ManageableUser> updateUser(@RequestBody ValtimoUser valtimoUser) {
        logger.debug("Request to update ValtimoUser : {}", valtimoUser);
        final ManageableUser user = userManagementService.updateUser(valtimoUser);
        final HttpHeaders headers = HeaderUtil.createAlert("userManagement.updated", user.getEmail());
        return ResponseEntity.ok().headers(headers).body(user);
    }

    @PutMapping("/v1/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable String userId) {
        logger.debug("Request to activate userId : {}", userId);
        userManagementService.activateUser(userId);
        final HttpHeaders headers = HeaderUtil.createAlert("userManagement.activated", userId);
        return ResponseEntity.ok().headers(headers).build();
    }

    @PutMapping("/v1/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable String userId) {
        logger.debug("Request to deactivate user : {}", userId);
        userManagementService.deactivateUser(userId);
        final HttpHeaders headers = HeaderUtil.createAlert("userManagement.deactivated", userId);
        return ResponseEntity.ok().headers(headers).build();
    }

    @GetMapping("/v1/users")
    public ResponseEntity<Page<ManageableUser>> getAllUsers(Pageable pageable) throws URISyntaxException {
        final Page<ManageableUser> page = userManagementService.getAllUsers(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping(value = "/v1/users", params = {"searchTerm"})
    public ResponseEntity<Page<ManageableUser>> queryUsers(@RequestParam("searchTerm") String searchTerm, Pageable pageable) {
        final Page<ManageableUser> page = userManagementService.queryUsers(searchTerm, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/v1/users/email/{email}/")
    public ResponseEntity<ManageableUser> getUserByEmail(@PathVariable String email) {
        logger.debug("Request to get user by email : {}", email);
        return userManagementService.findByEmail(email)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/users/{userId}")
    public ResponseEntity<ManageableUser> getUser(@PathVariable String userId) {
        logger.debug("Request to get user by id : {}", userId);
        final ManageableUser manageableUser = userManagementService.findById(userId);
        return ResponseEntity.ok(manageableUser);
    }

    @GetMapping("/v1/users/authority/{authority}")
    public ResponseEntity<List<ManageableUser>> getAllUsersByRole(@PathVariable String authority) {
        logger.debug("Request to get users by role : {}", authority);
        final List<ManageableUser> usersWithRole = userManagementService.findByRole(authority);
        return ResponseEntity.ok(usersWithRole);
    }

    @DeleteMapping("/v1/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        logger.debug("Request to delete user : {}", userId);
        userManagementService.deleteUser(userId);
        final HttpHeaders headers = HeaderUtil.createAlert("userManagement.deleted", userId);
        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/v1/users/send-verification-email/{userId}")
    public ResponseEntity<Void> resendVerificationEmail(@PathVariable String userId) {
        logger.debug("Request to resend verification email to user : {}", userId);
        boolean success = userManagementService.resendVerificationEmail(userId);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @GetMapping("/v1/user/settings")
    public ResponseEntity<String> getCurrentUserSettings(){
        logger.debug("Request to get current user settings");
        var result = userSettingsService.getCurrentUserSettings(userManagementService.getCurrentUser());
        return result.map(userSettings -> ResponseEntity.ok(userSettings.getUserProperties())).orElse(null);
    }

    @PostMapping("/v1/user/settings")
    public ResponseEntity<Object> saveCurrentUserSettings(@RequestBody String settings){
        logger.debug("Request to create settings for current user");
        try{
            JsonParser.parseString(settings);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        userSettingsService.saveUserSettings(userManagementService.getCurrentUser(),settings);
        return ResponseEntity.ok().build();

    }
}

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

package com.ritense.valtimo.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

class UserSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void createUserAsAdmin() throws Exception {
        assertHttpStatus(POST, "/api/users", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void createUserAsUser() throws Exception {
        assertHttpStatus(POST, "/api/users", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void updateUserAsAdmin() throws Exception {
        assertHttpStatus(PUT, "/api/users", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void updateUserAsUser() throws Exception {
        assertHttpStatus(PUT, "/api/users", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void activateUserAsAdmin() throws Exception {
        assertHttpStatus(PUT, String.format("/api/users/%s/activate", USER_ID), OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void activateUserAsUser() throws Exception {
        assertHttpStatus(PUT, String.format("/api/users/%s/activate", USER_ID), FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void deactivateUserAsAdmin() throws Exception {
        assertHttpStatus(PUT, String.format("/api/users/%s/deactivate", USER_ID), OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void deactivateUserAsUser() throws Exception {
        assertHttpStatus(PUT, String.format("/api/users/%s/deactivate", USER_ID), FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void getAllUsersAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/users", OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void getAllUsersAsUser() throws Exception {
        assertHttpStatus(GET, "/api/users", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void queryUsersAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/users", OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void queryUsersAsUser() throws Exception {
        assertHttpStatus(GET, "/api/users", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void getUserByEmailAsAdmin() throws Exception {
        assertHttpStatus(GET, String.format("/api/users/email/%s/", USER_EMAIL), NOT_FOUND);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void getUserByEmailAsUser() throws Exception {
        assertHttpStatus(GET, String.format("/api/users/email/%s/", USER_EMAIL), FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void getUserAsAdmin() throws Exception {
        assertHttpStatus(GET, String.format("/api/users/%s", USER_ID), OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void getUserAsUser() throws Exception {
        assertHttpStatus(GET, String.format("/api/users/%s", USER_ID), FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void getAllUsersByRoleAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/users/authority/USER", OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void getAllUsersByRoleAsUser() throws Exception {
        assertHttpStatus(GET, "/api/users/authority/USER", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void deleteUserAsAdmin() throws Exception {
        assertHttpStatus(DELETE, String.format("/api/users/%s", USER_ID), OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void deleteUserAsUser() throws Exception {
        assertHttpStatus(DELETE, String.format("/api/users/%s", USER_ID), FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void resendVerificationEmailAsAdmin() throws Exception {
        assertHttpStatus(POST, String.format("/api/users/send-verification-email/%s", USER_ID), BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void resendVerificationEmailAsUser() throws Exception {
        assertHttpStatus(POST, String.format("/api/users/send-verification-email/%s", USER_ID), FORBIDDEN);
    }

}
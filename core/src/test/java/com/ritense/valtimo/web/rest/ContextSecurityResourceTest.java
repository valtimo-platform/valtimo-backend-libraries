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

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

class ContextSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void updateContextAsAdmin() throws Exception {
        assertHttpStatus(PUT, "/api/v1/contexts", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void updateContextAsUser() throws Exception {
        assertHttpStatus(PUT, "/api/v1/contexts", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void createContextAsAdmin() throws Exception {
        assertHttpStatus(POST, "/api/v1/contexts", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = USER)
    void createContextAsUser() throws Exception {
        assertHttpStatus(POST, "/api/v1/contexts", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = ADMIN)
    void deleteChoiceFieldAsAdmin() throws Exception {
        assertHttpStatus(DELETE, "/api/v1/contexts/1", OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void deleteChoiceFieldAsUser() throws Exception {
        assertHttpStatus(DELETE, "/api/v1/contexts/1", FORBIDDEN);
    }

}
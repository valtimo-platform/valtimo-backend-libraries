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

package com.ritense.valtimo.web.rest;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

class ProcessSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void migrateProcessInstancesByProcessDefinitionIdsAsAdmin() throws Exception {
        assertHttpStatus(POST, "/api/v1/process/definition/sourceProcessDefinitionId/targetProcessDefinitionId/migrate", INTERNAL_SERVER_ERROR);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void migrateProcessInstancesByProcessDefinitionIdsAsUser() throws Exception {
        assertHttpStatus(POST, "/api/v1/process/definition/sourceProcessDefinitionId/targetProcessDefinitionId/migrate", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void deleteAsAdmin() throws Exception {
        assertHttpStatus(POST, "/api/v1/process/processInstanceId/delete", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void deleteAsUser() throws Exception {
        assertHttpStatus(POST, "/api/v1/process/processInstanceId/delete", FORBIDDEN);
    }

}
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

package com.ritense.valtimo.milestones.web.rest;

import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

class MilestoneSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void getMilestoneAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/v1/milestones/1", NOT_FOUND);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void getMilestoneAsUser() throws Exception {
        assertHttpStatus(GET, "/api/v1/milestones/1", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void listMilestonesAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/v1/milestones", OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void listMilestonesAsUser() throws Exception {
        assertHttpStatus(GET, "/api/v1/milestones", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void saveMilestoneAsAdmin() throws Exception {
        assertHttpStatus(POST, "/api/v1/milestones", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void saveMilestoneAsUser() throws Exception {
        assertHttpStatus(POST, "/api/v1/milestones", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void deleteMilestoneAsAdmin() throws Exception {
        assertHttpStatus(DELETE, "/api/v1/milestones/1", NO_CONTENT);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void deleteMilestoneAsUser() throws Exception {
        assertHttpStatus(DELETE, "/api/v1/milestones/1", FORBIDDEN);
    }

}
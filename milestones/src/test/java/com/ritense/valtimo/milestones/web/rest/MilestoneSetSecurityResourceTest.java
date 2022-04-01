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
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.DEVELOPER;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

class MilestoneSetSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void getMilestoneSetAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/milestone-sets/1", NOT_FOUND);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void getMilestoneSetAsUser() throws Exception {
        assertHttpStatus(GET, "/api/milestone-sets/1", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void listMilestoneSetsAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/milestone-sets", OK);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void listMilestoneSetsAsUser() throws Exception {
        assertHttpStatus(GET, "/api/milestone-sets", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {DEVELOPER})
    void saveMilestoneSetAsDeveloper() throws Exception {
        assertHttpStatus(POST, "/api/milestone-sets", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void saveMilestoneSetAsAdmin() throws Exception {
        assertHttpStatus(POST, "/api/milestone-sets", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void saveMilestoneSetAsUser() throws Exception {
        assertHttpStatus(POST, "/api/milestone-sets", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {DEVELOPER})
    void deleteMilestoneSetAsDeveloper() throws Exception {
        assertHttpStatus(DELETE, "/api/milestone-sets/1", NO_CONTENT);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void deleteMilestoneSetAsAdmin() throws Exception {
        assertHttpStatus(DELETE, "/api/milestone-sets/1", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void deleteMilestoneSetAsUser() throws Exception {
        assertHttpStatus(DELETE, "/api/milestone-sets/1", FORBIDDEN);
    }

}
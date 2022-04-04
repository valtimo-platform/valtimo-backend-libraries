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
import java.util.UUID;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

class MilestoneInstanceSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void getDiagramFlowNodesAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/milestones/" + UUID.randomUUID().toString() + "/flownodes", INTERNAL_SERVER_ERROR);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void getDiagramFlowNodesAsUser() throws Exception {
        assertHttpStatus(GET, "/api/milestones/" + UUID.randomUUID().toString() + "/flownodes", FORBIDDEN);
    }

}
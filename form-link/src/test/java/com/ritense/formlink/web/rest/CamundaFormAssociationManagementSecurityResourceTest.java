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

package com.ritense.formlink.web.rest;

import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
import static org.springframework.http.HttpStatus.NO_CONTENT;

class CamundaFormAssociationManagementSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void getAllAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/v1/form-association-management?processDefinitionKey=1", NOT_FOUND);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void getAllAsUser() throws Exception {
        assertHttpStatus(GET, "/api/v1/form-association-management?processDefinitionKey=1", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void getFormAssociationByIdAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/v1/form-association-management/formAssociationId", BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void getFormAssociationByIdAsUser() throws Exception {
        assertHttpStatus(GET, "/api/v1/form-association-management/formAssociationId", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void getFormAssociationByFormLinkIdAsAdmin() throws Exception {
        assertHttpStatus(GET, "/api/v1/form-association-management?processDefinitionKey=1&formLinkId=1", NO_CONTENT);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void getFormAssociationByFormLinkIdAsUser() throws Exception {
        assertHttpStatus(GET, "/api/v1/form-association-management?processDefinitionKey=1&formLinkId=1", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void createFormAssociationAsAdmin() throws Exception {
        var request = MockMvcRequestBuilders.request(POST, "/api/v1/form-association-management");
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        request.with(r -> {
            r.setRemoteAddr("8.8.8.8");
            return r;
        });
        assertHttpStatus(request, BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void createFormAssociationAsUser() throws Exception {
        var request = MockMvcRequestBuilders.request(POST, "/api/v1/form-association-management");
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        request.with(r -> {
            r.setRemoteAddr("8.8.8.8");
            return r;
        });

        assertHttpStatus(request, FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void modifyFormAssociationAsAdmin() throws Exception {
        var request = MockMvcRequestBuilders.request(PUT, "/api/v1/form-association-management");
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        request.with(r -> {
            r.setRemoteAddr("8.8.8.8");
            return r;
        });
        assertHttpStatus(request, BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void modifyFormAssociationAsUser() throws Exception {
        assertHttpStatus(PUT, "/api/v1/form-association-management", FORBIDDEN);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void deleteFormAssociationAsAdmin() throws Exception {
        assertHttpStatus(
            DELETE,
            "/api/v1/form-association-management/" + UUID.randomUUID().toString() + "/" + UUID.randomUUID().toString(),
            NO_CONTENT
        );
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void deleteFormAssociationAsUser() throws Exception {
        assertHttpStatus(
            DELETE,
            "/api/v1/form-association-management/" + UUID.randomUUID().toString() + "/" + UUID.randomUUID().toString(),
            FORBIDDEN
        );
    }
}
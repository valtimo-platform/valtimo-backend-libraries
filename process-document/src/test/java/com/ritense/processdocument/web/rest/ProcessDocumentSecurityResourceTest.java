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

package com.ritense.processdocument.web.rest;

import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

class ProcessDocumentSecurityResourceTest extends SecuritySpecificEndpointIntegrationTest {

    @MockBean
    private ResourceService resourceService;

    private static final String USER_EMAIL = "user@valtimo.nl";

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {ADMIN})
    void createProcessDocumentDefinitionAsAdmin() throws Exception {
        var request = MockMvcRequestBuilders.request(POST, "/api/process-document/definition");
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
    void createProcessDocumentDefinitionAsUser() throws Exception {
        var request = MockMvcRequestBuilders.request(POST, "/api/process-document/definition");

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
    void deleteProcessDocumentDefinitionAsAdmin() throws Exception {
        var request = MockMvcRequestBuilders.request(DELETE, "/api/process-document/definition");

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
    void deleteProcessDocumentDefinitionAsUser() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(DELETE, "/api/process-document/definition");

        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);

        request.with(r -> {
            r.setRemoteAddr("8.8.8.8");
            return r;
        });

        assertHttpStatus(request, FORBIDDEN);
    }

}
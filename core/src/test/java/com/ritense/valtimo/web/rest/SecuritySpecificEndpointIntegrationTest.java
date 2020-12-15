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

import com.ritense.valtimo.contract.authentication.UserManagementService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Tag("security")
public abstract class SecuritySpecificEndpointIntegrationTest {

    @MockBean
    private UserManagementService userManagementService;

    @Autowired
    private MockMvc mockMvc;

    protected void assertHttpStatus(
        HttpMethod method,
        String path,
        HttpStatus shouldBeStatus
    ) throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, path);
        request.accept(MediaType.ALL);
        request.with(r -> {
            r.setRemoteAddr("8.8.8.8");
            return r;
        });
        assertHttpStatus(request, shouldBeStatus);
    }

    protected void assertHttpStatus(
        MockHttpServletRequestBuilder request,
        HttpStatus shouldBeStatus
    ) throws Exception {
        MvcResult mvcResult = mockMvc.perform(request).andReturn();
        int statusCode = mvcResult.getResponse().getStatus();
        assertThat(statusCode).isEqualTo(shouldBeStatus.value());
    }
}
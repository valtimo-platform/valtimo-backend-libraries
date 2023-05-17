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

import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserResourceIntTest extends BaseIntegrationTest {
    @Autowired
    private UserResource userResource;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

    @Test
    void shouldCreateAndRetrieveUserSettings() throws Exception {
        when(userManagementService.getCurrentUser()).thenReturn(getUser());
        mockMvc.perform(
                put("/api/v1/user/settings")
                    .accept(APPLICATION_JSON_VALUE)
                    .content(buildJson())
            )
            .andDo(print())
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/user/settings"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.property").value("value"))
            .andExpect(jsonPath("$.key").value("otherValue"));
    }

    private ManageableUser getUser() {
        var user = new ValtimoUser();
        user.setId(UUID.randomUUID().toString());
        return user;
    }

    private String buildJson() {
        return
            "{\"property\": \"value\",\"key\": \"otherValue\"}".trim();
    }
}

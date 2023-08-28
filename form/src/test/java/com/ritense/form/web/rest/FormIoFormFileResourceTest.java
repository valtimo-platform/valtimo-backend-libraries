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

package com.ritense.form.web.rest;

import com.ritense.form.BaseTest;
import com.ritense.form.web.rest.impl.FormIoFormFileResource;
import com.ritense.resource.service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.net.URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class FormIoFormFileResourceTest extends BaseTest {

    private FormIoFormFileResource formIoFormFileResource;
    private ResourceService resourceService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        resourceService = mock(ResourceService.class);
        formIoFormFileResource = new FormIoFormFileResource(resourceService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(formIoFormFileResource)
            .build();
    }

    @Test
    public void shouldReturn302FoundGettingFile() throws Exception {
        when(resourceService.getResourceUrl(anyString())).thenReturn(new URL("http://www.nu.nl"));

        mockMvc.perform(get("/api/v1/form-file").param("form", "aKeyValue")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isFound());
    }

}
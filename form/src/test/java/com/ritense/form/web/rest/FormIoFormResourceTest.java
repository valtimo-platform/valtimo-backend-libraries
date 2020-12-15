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

package com.ritense.form.web.rest;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.form.BaseTest;
import com.ritense.form.service.impl.FormIoFormLoaderService;
import com.ritense.form.web.rest.impl.FormIoFormResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class FormIoFormResourceTest extends BaseTest {

    private FormIoFormResource formIoFormResource;

    @MockBean
    private FormIoFormLoaderService formIoFormLoaderService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        formIoFormLoaderService = mock(FormIoFormLoaderService.class);
        formIoFormResource = new FormIoFormResource(formIoFormLoaderService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(formIoFormResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    public void shouldReturn200WithJson() throws Exception {
        final var formIoFormDefinition = formDefinition();
        when(formIoFormLoaderService.getFormDefinitionByName(anyString())).thenReturn(Optional.of(formIoFormDefinition.asJson()));

        mockMvc.perform(get("/api/form/{formDefinitionName}", "someName")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void shouldReturn204WithNoJson() throws Exception {
        when(formIoFormLoaderService.getFormDefinitionByName(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/form/{formDefinitionName}", "someName")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturn200WithDataFilledJson() throws Exception {
        final var formIoFormDefinition = formDefinition();
        when(formIoFormLoaderService.getFormDefinitionByNamePreFilled(anyString(), any(JsonSchemaDocumentId.class)))
            .thenReturn(Optional.of(formIoFormDefinition.asJson()));

        mockMvc.perform(get("/api/form/{formDefinitionName}/document/{documentId}", "formDefinitionName", UUID.randomUUID())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void shouldReturn204WithNoDataFilledJson() throws Exception {
        when(formIoFormLoaderService.getFormDefinitionByNamePreFilled(anyString(), any(JsonSchemaDocumentId.class))).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/form/{formDefinitionName}/document/{documentId}", "formDefinitionName", UUID.randomUUID())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent());
    }

}
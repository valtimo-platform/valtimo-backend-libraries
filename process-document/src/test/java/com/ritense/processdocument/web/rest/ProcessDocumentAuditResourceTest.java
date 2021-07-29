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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.domain.MetaData;
import com.ritense.audit.domain.MetaDataBuilder;
import com.ritense.processdocument.BaseTest;
import com.ritense.processdocument.domain.event.TestEvent;
import com.ritense.processdocument.service.ProcessDocumentAuditService;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.json.serializer.PageSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ProcessDocumentAuditResourceTest extends BaseTest {

    @MockBean
    private ProcessDocumentAuditService processDocumentAuditService;
    private ProcessDocumentAuditResource processDocumentAuditResource;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        processDocumentAuditService = mock(ProcessDocumentAuditService.class);
        processDocumentAuditResource = new ProcessDocumentAuditResource(processDocumentAuditService);
        mockMvc = MockMvcBuilders.standaloneSetup(processDocumentAuditResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(jacksonMessageConverter())
            .build();
    }

    @Test
    public void shouldReturnMetaDataAndAuditEventWithoutOriginField() throws Exception {
        final TestEvent event = testEvent(UUID.randomUUID(), LocalDateTime.now());
        final MetaData metaData = metaData(event);
        final AuditRecord auditRecord = auditRecord(event, metaData);

        when(processDocumentAuditService.getAuditLog(any(), any()))
            .thenReturn(new PageImpl<>(List.of(auditRecord)));

        mockMvc.perform(
            get("/api/process-document/instance/document/{documentId}/audit", UUID.randomUUID().toString())
                .characterEncoding(StandardCharsets.UTF_8.name())
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isNotEmpty())
            .andExpect(jsonPath("$.content[0].metaData.origin").doesNotExist())
            .andExpect(jsonPath("$.content[0].auditEvent.origin").doesNotExist());
    }

    private AuditRecord auditRecord(AuditEvent event, MetaData metaData) {
        return AuditRecord.builder()
            .id(event.getId())
            .metaData(metaData)
            .auditEvent(event)
            .build();
    }

    private MetaData metaData(AuditEvent event) {
        return new MetaDataBuilder()
            .origin(event.getOrigin())
            .occurredOn(event.getOccurredOn())
            .user(event.getUser())
            .build();
    }

    private TestEvent testEvent(UUID uuid, LocalDateTime localDateTime) {
        return new TestEvent(
            uuid,
            "somewhere",
            localDateTime,
            "somebody",
            "John Doe",
            21,
            "USA",
            "M",
            "myProcessInstanceId"
        );
    }

    private MappingJackson2HttpMessageConverter jacksonMessageConverter() {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
            .failOnUnknownProperties(false)
            .serializerByType(Page.class, new PageSerializer()).build();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

}
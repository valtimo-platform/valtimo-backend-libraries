/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.case_.domain.definition.CaseDefinition;
import com.ritense.case_.service.ActiveCaseDefinitionService;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.processdocument.BaseTest;
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinition;
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinitionId;
import com.ritense.processdocument.domain.ProcessDefinitionId;
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.ProcessDocumentInstanceDto;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService;
import com.ritense.processdocument.service.impl.OperatonProcessJsonSchemaDocumentAssociationService;
import com.ritense.processdocument.service.impl.OperatonProcessJsonSchemaDocumentService;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndCompleteTaskResultSucceeded;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndStartProcessResultSucceeded;
import com.ritense.processdocument.service.impl.result.NewDocumentAndStartProcessResultSucceeded;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import com.ritense.valtimo.contract.json.MapperSingleton;
import com.ritense.valtimo.contract.utils.TestUtil;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class ProcessDocumentResourceTest extends BaseTest {
    private static final String PROCESS_INSTANCE_ID = UUID.randomUUID().toString();

    @MockitoBean
    private OperatonProcessJsonSchemaDocumentService processDocumentService;

    @MockitoBean
    private OperatonProcessJsonSchemaDocumentAssociationService processDocumentAssociationService;

    private MockMvc mockMvc;
    private ProcessDefinitionCaseDefinition processDefinitionCaseDefinition;
    private ProcessDocumentInstanceDto processDocumentInstance;
    private ObjectMapper objectMapper;
    private ProcessDefinitionCaseDefinitionService processDefinitionCaseDefinitionService;
    private ActiveCaseDefinitionService activeCaseDefinitionService;

    private CaseDefinitionId caseDefinitionId;
    private CaseDefinition caseDefinition;

    @BeforeEach
    void setUp() {
        objectMapper = MapperSingleton.INSTANCE.get();
        processDocumentService = mock(OperatonProcessJsonSchemaDocumentService.class);
        processDocumentAssociationService = mock(OperatonProcessJsonSchemaDocumentAssociationService.class);
        processDefinitionCaseDefinitionService = mock(ProcessDefinitionCaseDefinitionService.class);
        activeCaseDefinitionService = mock(ActiveCaseDefinitionService.class);
        ProcessDocumentResource processDocumentResource = new ProcessDocumentResource(
            processDocumentService,
            processDocumentAssociationService,
            processDefinitionCaseDefinitionService,
            activeCaseDefinitionService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(processDocumentResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(MapperSingleton.INSTANCE.get()))
            .build();

        processDefinitionCaseDefinition = new ProcessDefinitionCaseDefinition(
            new ProcessDefinitionCaseDefinitionId(
                new ProcessDefinitionId("test"),
                CASE_DEFINITION_ID
            ),
            false,
            false
        );

        processDocumentInstance = new ProcessDocumentInstanceDto(
            OperatonProcessJsonSchemaDocumentInstanceId.newId(
                new OperatonProcessInstanceId(PROCESS_INSTANCE_ID),
                JsonSchemaDocumentId.existingId(UUID.randomUUID())
            ),
            "aName",
            true,
            1,
            2,
            "John Doe",
            LocalDateTime.parse("2024-01-01T12:10:00")
        );

        caseDefinitionId = new CaseDefinitionId("house", "1.0.0");
        caseDefinition = new CaseDefinition(
            caseDefinitionId,
            "house",
            null,
            null,
            null,
            null,
            true,
            true,
            true,
            false,
            false,
            null,
            null
        );
        when(activeCaseDefinitionService.getActiveCaseDefinition("house")).thenReturn(caseDefinition);
    }

    @Test
    void shouldReturnOkWhenGettingProcessDocumentDefinition() throws Exception {
        when(processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(eq(caseDefinitionId), isNull(), isNull()))
            .thenReturn(List.of(processDefinitionCaseDefinition));

        mockMvc.perform(
                get("/api/v1/case-definition/{caseDefinitionKey}/case-process-link", DOCUMENT_DEFINITION_NAME)
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[0].id.processDefinitionId").value("test"))
            .andExpect(jsonPath("$.[0].id.caseDefinitionId.key").value("house"))
            .andExpect(jsonPath("$.[0].id.caseDefinitionId.versionTag").value("1.0.0"))
            .andExpect(jsonPath("$.[0].canInitializeDocument").value(false))
            .andExpect(jsonPath("$.[0].startableByUser").value(false));
    }

    @Test
    void shouldReturnOkWhenGettingProcessDocumentDefinitionByProcessInstanceId() throws Exception {
        when(processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinition(new OperatonProcessInstanceId(PROCESS_INSTANCE_ID)))
            .thenReturn(processDefinitionCaseDefinition);

        mockMvc.perform(
                get("/api/v1/process-instance/{processInstanceId}/case-process-link", PROCESS_INSTANCE_ID)
                    .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id.processDefinitionId").value("test"))
            .andExpect(jsonPath("$.id.caseDefinitionId.key").value("house"))
            .andExpect(jsonPath("$.id.caseDefinitionId.versionTag").value("1.0.0"))
            .andExpect(jsonPath("$.canInitializeDocument").value(false))
            .andExpect(jsonPath("$.startableByUser").value(false));
    }

    @Test
    void shouldReturnOkWhenGettingProcessDocumentInstances() throws Exception {
        when(processDocumentAssociationService.findProcessDocumentInstanceDtos(any(JsonSchemaDocumentId.class)))
            .thenReturn(List.of(processDocumentInstance));

        mockMvc.perform(
                get("/api/v1/process-document/instance/document/{documentId}", UUID.randomUUID().toString())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[0].id.processInstanceId").exists())
            .andExpect(jsonPath("$.[0].id.documentId").exists())
            .andExpect(jsonPath("$.[0].active").value(true))
            .andExpect(jsonPath("$.[0].version").value(1))
            .andExpect(jsonPath("$.[0].latestVersion").value(2))
            .andExpect(jsonPath("$.[0].startedBy").value("John Doe"))
            .andExpect(jsonPath("$.[0].startedOn").value("2024-01-01T12:10:00.000Z"));
    }

    @Test
    void shouldReturnOkWhenCreatingNewDocumentAndStartProcess() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Funenparks\"}");
        final CreateDocumentResult result = createDocument(definition(), content);

        final OperatonProcessInstanceId processInstanceId = new OperatonProcessInstanceId(UUID.randomUUID().toString());
        var resultSucceeded = new NewDocumentAndStartProcessResultSucceeded(
            result.resultingDocument().orElseThrow(),
            processInstanceId
        );

        when(processDocumentService.newDocumentAndStartProcess(any())).thenReturn(resultSucceeded);

        final JsonNode jsonContent = objectMapper.readTree("{\"street\": \"Funenparks\"}");
        var newDocumentRequest = new NewDocumentRequest(
            "house",
            "house",
            "1.0.0",
            jsonContent
        );
        var request = new NewDocumentAndStartProcessRequest("some-key", newDocumentRequest);

        mockMvc.perform(
                post("/api/v1/process-document/operation/new-document-and-start-process")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.document").exists())
            .andExpect(jsonPath("$.processInstanceId").value(processInstanceId.toString()))
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldReturnOkWhenModifyDocumentAndCompleteTask() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Funenparks\"}");
        final CreateDocumentResult result = createDocument(definition(), content);

        var resultSucceeded = new ModifyDocumentAndCompleteTaskResultSucceeded(result.resultingDocument().orElseThrow());
        when(processDocumentService.modifyDocumentAndCompleteTask(any())).thenReturn(resultSucceeded);

        final JsonNode jsonDataUpdate = objectMapper.readTree("{\"street\": \"Funenparks\"}");
        var modifyRequest = new ModifyDocumentRequest(
            UUID.randomUUID().toString(),
            jsonDataUpdate
        );
        var request = new ModifyDocumentAndCompleteTaskRequest(modifyRequest, "task-id");

        mockMvc.perform(
                post("/api/v1/process-document/operation/modify-document-and-complete-task")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.document").exists())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldReturnOkWhenModifyDocumentAndStartProcess() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Funenparks\"}");
        final CreateDocumentResult result = createDocument(definition(), content);

        final var operatonProcessInstanceId = new OperatonProcessInstanceId(UUID.randomUUID().toString());
        var resultSucceeded = new ModifyDocumentAndStartProcessResultSucceeded(
            result.resultingDocument().orElseThrow(), operatonProcessInstanceId);
        when(processDocumentService.modifyDocumentAndStartProcess(any())).thenReturn(resultSucceeded);

        final JsonNode jsonDataUpdate = objectMapper.readTree("{\"street\": \"Funenparks\"}");
        var modifyRequest = new ModifyDocumentRequest(
            UUID.randomUUID().toString(),
            jsonDataUpdate
        );
        var request = new ModifyDocumentAndStartProcessRequest("some-key", modifyRequest);

        mockMvc.perform(
                post("/api/v1/process-document/operation/modify-document-and-start-process")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.document").exists())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isEmpty());
    }
}

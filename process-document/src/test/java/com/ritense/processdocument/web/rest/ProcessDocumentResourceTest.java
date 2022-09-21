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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.processdocument.BaseTest;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcess;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessLinkResponse;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentAssociationService;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndCompleteTaskResultSucceeded;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndStartProcessResultSucceeded;
import com.ritense.processdocument.service.impl.result.NewDocumentAndStartProcessResultSucceeded;
import com.ritense.valtimo.contract.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ProcessDocumentResourceTest extends BaseTest {
    private static final String PROCESS_DEFINITION_KEY = "definition-id";
    private static final String PROCESS_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String DOCUMENT_DEFINITION_NAME = "house";

    @MockBean
    private CamundaProcessJsonSchemaDocumentService processDocumentService;

    @MockBean
    private CamundaProcessJsonSchemaDocumentAssociationService processDocumentAssociationService;

    private DocumentDefinitionProcessLinkService documentDefinitionProcessLinkService;
    private MockMvc mockMvc;
    private CamundaProcessJsonSchemaDocumentDefinition processDocumentDefinition;
    private CamundaProcessJsonSchemaDocumentInstance processDocumentInstance;
    private Page<CamundaProcessJsonSchemaDocumentDefinition> processDocumentInstancesPage;
    private ObjectMapper objectMapper;
    private JsonSchemaDocumentDefinitionId documentDefinitionId;
    private CamundaProcessDefinitionKey processDefinitionKey;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        processDocumentService = mock(CamundaProcessJsonSchemaDocumentService.class);
        processDocumentAssociationService = mock(CamundaProcessJsonSchemaDocumentAssociationService.class);
        documentDefinitionProcessLinkService = mock(DocumentDefinitionProcessLinkService.class);
        ProcessDocumentResource processDocumentResource = new ProcessDocumentResource(
            processDocumentService,
            processDocumentAssociationService,
            documentDefinitionProcessLinkService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(processDocumentResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        documentDefinitionId = JsonSchemaDocumentDefinitionId.newId(DOCUMENT_DEFINITION_NAME);
        processDefinitionKey = new CamundaProcessDefinitionKey(PROCESS_DEFINITION_KEY);

        processDocumentDefinition = new CamundaProcessJsonSchemaDocumentDefinition(
            CamundaProcessJsonSchemaDocumentDefinitionId.newId(
                processDefinitionKey,
                documentDefinitionId
            ),
            false,
            false
        );

        processDocumentInstance = new CamundaProcessJsonSchemaDocumentInstance(
            CamundaProcessJsonSchemaDocumentInstanceId.newId(
                new CamundaProcessInstanceId(PROCESS_INSTANCE_ID),
                JsonSchemaDocumentId.existingId(UUID.randomUUID())
            ),
            "aName"
        );

        List<CamundaProcessJsonSchemaDocumentDefinition> camundaProcessJsonSchemaDocumentDefinitions = List.of(
            processDocumentDefinition
        );
        Pageable unpaged = Pageable.unpaged();
        processDocumentInstancesPage = new PageImpl<>(camundaProcessJsonSchemaDocumentDefinitions, unpaged, 1);
    }

    @Test
    void shouldReturnOkWhenGettingProcessDocumentDefinitionsPaged() throws Exception {
        when(processDocumentAssociationService.getAllProcessDocumentDefinitions(any())).thenReturn(processDocumentInstancesPage);

        mockMvc.perform(
                get("/api/process-document/definition")
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.content[0].id.processDefinitionKey").exists())
            .andExpect(jsonPath("$.content[0].id.documentDefinitionId.name").exists())
            .andExpect(jsonPath("$.content[0].id.documentDefinitionId.version").exists());
    }

    @Test
    void shouldReturnOkWhenGettingProcessDocumentDefinition() throws Exception {
        when(processDocumentAssociationService.findProcessDocumentDefinitions(eq(documentDefinitionId.name())))
            .thenReturn(List.of(processDocumentDefinition));

        mockMvc.perform(
                get("/api/process-document/definition/document/{document-definition-name}", DOCUMENT_DEFINITION_NAME)
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[0].id.processDefinitionKey").value(processDefinitionKey.toString()))
            .andExpect(jsonPath("$.[0].id.documentDefinitionId.name").value(documentDefinitionId.name()))
            .andExpect(jsonPath("$.[0].id.documentDefinitionId.version").value(documentDefinitionId.version()));
    }

    @Test
    void shouldReturnOkWhenGettingProcessDocumentInstances() throws Exception {
        when(processDocumentAssociationService.findProcessDocumentInstances(any(JsonSchemaDocumentId.class)))
            .thenReturn(List.of(processDocumentInstance));

        mockMvc.perform(
                get("/api/process-document/instance/document/{documentId}", UUID.randomUUID().toString())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[0].id.processInstanceId").exists())
            .andExpect(jsonPath("$.[0].id.documentId").exists());
    }

    @Test
    void shouldReturnOkWhenCreatingNewDocumentAndStartProcess() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Funenparks\"}");
        final CreateDocumentResult result = createDocument(definition(), content);

        final CamundaProcessInstanceId processInstanceId = new CamundaProcessInstanceId(UUID.randomUUID().toString());
        var resultSucceeded = new NewDocumentAndStartProcessResultSucceeded(
            result.resultingDocument().orElseThrow(),
            processInstanceId
        );

        when(processDocumentService.newDocumentAndStartProcess(any())).thenReturn(resultSucceeded);

        final JsonNode jsonContent = objectMapper.readTree("{\"street\": \"Funenparks\"}");
        var newDocumentRequest = new NewDocumentRequest(
            "house",
            jsonContent
        );
        var request = new NewDocumentAndStartProcessRequest("some-key", newDocumentRequest);

        mockMvc.perform(
                post("/api/process-document/operation/new-document-and-start-process")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
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
            jsonDataUpdate,
            "1"
        );
        var request = new ModifyDocumentAndCompleteTaskRequest(modifyRequest, "task-id");

        mockMvc.perform(
                post("/api/process-document/operation/modify-document-and-complete-task")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.document").exists())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldReturnOkWhenCreatingProcessDocumentDefinition() throws Exception {
        final var processDefinitionKey = new CamundaProcessDefinitionKey("some-key");
        final var documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId("house", 1);
        final var request = new ProcessDocumentDefinitionRequest(
            processDefinitionKey.toString(),
            documentDefinitionId.name(),
            false,
            false
        );

        final var camundaProcessJsonSchemaDocumentDefinition = new CamundaProcessJsonSchemaDocumentDefinition(
            CamundaProcessJsonSchemaDocumentDefinitionId.newId(processDefinitionKey, documentDefinitionId),
            false,
            false
        );
        when(processDocumentAssociationService.createProcessDocumentDefinition(any()))
            .thenReturn(Optional.of(camundaProcessJsonSchemaDocumentDefinition));

        mockMvc.perform(
                post("/api/process-document/definition")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingProcessDocumentDefinition() throws Exception {
        final var request = new ProcessDocumentDefinitionRequest(null, null, false, false);

        when(processDocumentAssociationService.createProcessDocumentDefinition(any())).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/api/process-document/definition")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenModifyDocumentAndStartProcess() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Funenparks\"}");
        final CreateDocumentResult result = createDocument(definition(), content);

        final var camundaProcessInstanceId = new CamundaProcessInstanceId(UUID.randomUUID().toString());
        var resultSucceeded = new ModifyDocumentAndStartProcessResultSucceeded(
            result.resultingDocument().orElseThrow(), camundaProcessInstanceId);
        when(processDocumentService.modifyDocumentAndStartProcess(any())).thenReturn(resultSucceeded);

        final JsonNode jsonDataUpdate = objectMapper.readTree("{\"street\": \"Funenparks\"}");
        var modifyRequest = new ModifyDocumentRequest(
            UUID.randomUUID().toString(),
            jsonDataUpdate,
            "1"
        );
        var request = new ModifyDocumentAndStartProcessRequest("some-key", modifyRequest);

        mockMvc.perform(
                post("/api/process-document/operation/modify-document-and-start-process")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.document").exists())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldDeleteProcessDocumentDefinition() throws Exception {
        final var request = new ProcessDocumentDefinitionRequest(
            "some-key",
            "documentDefinitionName",
            false,
            false
        );
        mockMvc.perform(
                delete("/api/process-document/definition")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isNoContent());
        verify(processDocumentAssociationService).deleteProcessDocumentDefinition((ProcessDocumentDefinitionRequest) any());
    }

    @Test
    void shouldGetDocumentDefinitionProcesses() throws Exception {
        String documentDefinitionName = "name";
        DocumentDefinitionProcess documentDefinitionProcess = new DocumentDefinitionProcess(
            "processId",
            "processName"
        );
        when(documentDefinitionProcessLinkService.getDocumentDefinitionProcess(documentDefinitionName))
            .thenReturn(documentDefinitionProcess);

        mockMvc.perform(
                get("/api/process-document/demo/{name}/process", documentDefinitionName))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.processId").value("processId"))
            .andExpect(jsonPath("$.processName").value("processName"));

        verify(documentDefinitionProcessLinkService).getDocumentDefinitionProcess(documentDefinitionName);
    }

    @Test
    void shouldPutDocumentDefinitionProcesses() throws Exception {
        String documentDefinitionName = "name";
        DocumentDefinitionProcessLinkResponse response = new DocumentDefinitionProcessLinkResponse(
            "processId",
            "processName"
        );
        DocumentDefinitionProcessRequest request = new DocumentDefinitionProcessRequest("processId");

        when(documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(eq(documentDefinitionName), any()))
            .thenReturn(response);

        mockMvc.perform(
                put("/api/process-document/demo/{name}/process", documentDefinitionName)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(request))
                    .characterEncoding(StandardCharsets.UTF_8.name()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.processId").value("processId"))
            .andExpect(jsonPath("$.processName").value("processName"));

        verify(documentDefinitionProcessLinkService).saveDocumentDefinitionProcess(eq(documentDefinitionName), any());
    }

    @Test
    void shouldDeleteDocumentDefinitionProcesses() throws Exception {
        String documentDefinitionName = "name";

        mockMvc.perform(delete("/api/process-document/demo/{name}/process", documentDefinitionName))
            .andDo(print())
            .andExpect(status().isOk());

        verify(documentDefinitionProcessLinkService).deleteDocumentDefinitionProcess(documentDefinitionName);
    }

}
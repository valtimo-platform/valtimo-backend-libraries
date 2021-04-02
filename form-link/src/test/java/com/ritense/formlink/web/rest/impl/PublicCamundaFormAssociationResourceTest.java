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

package com.ritense.formlink.web.rest.impl;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.document.service.DocumentService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseIntegrationTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociationId;
import com.ritense.formlink.domain.impl.formassociation.FormAssociations;
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import com.ritense.formlink.repository.ProcessFormAssociationRepository;
import com.ritense.valtimo.task.publictask.PublicTaskRequest;
import com.ritense.valtimo.task.publictask.PublicTaskTokenService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@Tag("integration")
@WebAppConfiguration
public class PublicCamundaFormAssociationResourceTest extends BaseIntegrationTest {

    @Inject
    private PublicCamundaFormAssociationResource publicCamundaFormAssociationResource;

    @Inject
    private PublicTaskTokenService publicTaskTokenService;

    @Inject
    private DocumentSequenceGeneratorService documentSequenceGeneratorService;

    @MockBean
    private ProcessFormAssociationRepository processFormAssociationRepository;

    @MockBean
    private FormIoFormDefinitionService formDefinitionService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private TaskService taskService;

    private MockMvc mockMvc;
    public static final String PROCESS_DEFINITION_KEY = "Test123";
    public static final String FORM_LINK_ID = "Test456";
    public static final String DOCUMENT_ID = UUID.randomUUID().toString();
    public static final String TASK_INSTANCE_ID = "Test101112";
    public static final UUID FORM_ID = UUID.randomUUID();


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicCamundaFormAssociationResource).build();
    }

    @Test
    void getFormDefinitionByFormLinkIdAndSucceed() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, true);

        mockFormAssociation(formLink);
        mockDocument();

        String tokenForTask = getTaskToken();

        mockMvc.perform(get("/api/public/form-association/form-definition").header("Authorization", tokenForTask))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void getFormDefinitionByFormLinkIdNonPublicTaskForbidden() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, false);

        mockFormAssociation(formLink);

        String tokenForTask = getTaskToken();

        mockMvc.perform(get("/api/public/form-association/form-definition").header("Authorization", tokenForTask))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    void getFormDefinitionByFormLinkIdForgedTokenForbidden() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, false);

        mockFormAssociation(formLink);

        String tokenForTask = getForgedTaskToken(PROCESS_DEFINITION_KEY, FORM_LINK_ID);

        mockMvc.perform(get("/api/public/form-association/form-definition").header("Authorization", tokenForTask))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    void getPreFilledFormDefinitionByFormLinkIdAndSucceed() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, true);

        mockFormAssociation(formLink);

        mockDocument();

        String tokenForTask = getTaskToken();

        mockMvc.perform(get("/api/public/form-association/form-definition")
            .header("Authorization", tokenForTask)
            .param("documentId", UUID.randomUUID().toString()))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void getPreFilledFormDefinitionByFormLinkIdNonPublicTaskForbidden() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, false);

        mockFormAssociation(formLink);

        mockDocument();

        String tokenForTask = getTaskToken();

        mockMvc.perform(get("/api/public/form-association/form-definition").header("Authorization", tokenForTask)
            .param("documentId", UUID.randomUUID().toString()))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    void getPreFilledFormDefinitionByFormLinkIdForgedTokenForbidden() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, true);

        mockFormAssociation(formLink);

        mockDocument();

        String tokenForTask = getForgedTaskToken(PROCESS_DEFINITION_KEY, FORM_LINK_ID);

        mockMvc.perform(get("/api/public/form-association/form-definition").header("Authorization", tokenForTask)
            .param("documentId", UUID.randomUUID().toString()))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    private void mockFormAssociation(BpmnElementFormIdLink formLink) {
        CamundaProcessFormAssociation formAssociation = new CamundaProcessFormAssociation(
            CamundaProcessFormAssociationId.newId(UUID.nameUUIDFromBytes(new byte[] {0})),
            PROCESS_DEFINITION_KEY,
            new FormAssociations(
                Collections.singletonList(new UserTaskFormAssociation(
                        UUID.nameUUIDFromBytes(new byte[] {0}),
                        formLink
                    )
                )
            )
        );

        when(processFormAssociationRepository.findByProcessDefinitionKey(eq(PROCESS_DEFINITION_KEY))).thenReturn(
            Optional.of(formAssociation));

        FormIoFormDefinition formDefinition = new FormIoFormDefinition(UUID.randomUUID(),
            "",
            "{}",
            false
        );
        doReturn(Optional.of(formDefinition)).when(formDefinitionService).getFormDefinitionById(eq(FORM_ID));

        var taskQuery = mock(TaskQuery.class);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.taskId(eq(TASK_INSTANCE_ID))).thenReturn(taskQuery);
        when(taskQuery.count()).thenReturn(1L);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
    }

    private String getTaskToken() {
        PublicTaskRequest claims = new PublicTaskRequest.PublicTaskRequestBuilder()
            .setProcessDefinitionKey(PROCESS_DEFINITION_KEY)
            .setFormLinkId(FORM_LINK_ID)
            .setDocumentId(DOCUMENT_ID)
            .setTaskInstanceId(TASK_INSTANCE_ID)
            .createPublicTaskTokenClaims();

        return publicTaskTokenService.getTokenForTask(claims);
    }

    private void mockDocument() {
        final var jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("house");
        final var jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        final var definition = new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
        var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final JsonSchemaDocument.CreateDocumentResultImpl result = JsonSchemaDocument.create(
            definition,
            content,
            "test@test.com",
            documentSequenceGeneratorService,
            null
        );

        var document = result.resultingDocument().orElseThrow();
        doReturn(Optional.of(document)).when(documentService).findBy(any());
    }

    public URI path(String name) {
        return URI.create(String.format("config/document/definition/%s.json", name + ".schema"));
    }

}
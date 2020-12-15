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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.document.service.DocumentService;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseIntegrationTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentAssociationService;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndCompleteTaskResultSucceeded;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.task.publictask.PublicTaskRequest;
import com.ritense.valtimo.task.publictask.PublicTaskTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@Tag("integration")
@WebAppConfiguration
public class PublicCamundaFormAssociationResourceSubmissionTest extends BaseIntegrationTest {

    @Inject
    private PublicCamundaFormAssociationResource publicCamundaFormAssociationResource;

    @Inject
    private PublicTaskTokenService publicTaskTokenService;

    @Inject
    private DocumentSequenceGeneratorService documentSequenceGeneratorService;

    @MockBean
    private FormIoFormDefinitionService formDefinitionService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private CamundaProcessJsonSchemaDocumentService processDocumentService;

    @MockBean
    private CamundaFormAssociationService formAssociationService;

    @MockBean
    private CamundaTaskService camundaTaskService;

    @MockBean
    private CamundaProcessJsonSchemaDocumentAssociationService processDocumentAssociationService;

    private MockMvc mockMvc;
    public static final String PROCESS_DEFINITION_KEY = "Test123";
    public static final String FORM_LINK_ID = "Test456";
    public static final String DOCUMENT_ID = UUID.randomUUID().toString();
    public static final String TASK_INSTANCE_ID = UUID.randomUUID().toString();
    public static final UUID FORM_ID = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicCamundaFormAssociationResource).build();
    }

    @Test
    public void handleSubmissionAndSucceed() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, true);

        ObjectNode formData = JsonNodeFactory.instance.objectNode();
        formData.put("street", "value");

        mockSubmissionData(formLink, formData);
        mockDocument();
        mockTaskInstanceVars();
        mockProcessDocumentDefinition();
        String tokenForTask = getTaskToken();

        mockMvc.perform(
            post("/api/public/form-association/form-definition/submission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", tokenForTask)
                .content(formData.toString())
        )
            .andDo(print())
            .andExpect(status().isOk());
    }

    private void mockProcessDocumentDefinition() {
        when(processDocumentAssociationService.findProcessDocumentDefinition(any(), anyLong())).thenReturn(
            Optional.of(processDocumentDefinition("aName"))
        );
    }

    private void mockTaskInstanceVars() {
        when(camundaTaskService.getTaskVariables(any())).thenReturn(Map.of());
    }

    @Test
    public void handleSubmissionNonPublicTaskForbidden() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, false);

        ObjectNode formData = JsonNodeFactory.instance.objectNode();
        formData.put("street", "value");

        mockSubmissionData(formLink, formData);
        mockDocument();
        String tokenForTask = getTaskToken();

        mockMvc.perform(
            post("/api/public/form-association/form-definition/submission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", tokenForTask)
                .content(formData.toString())
        )
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    public void handleSubmissionForgedTokenForbidden() throws Exception {
        BpmnElementFormIdLink formLink = new BpmnElementFormIdLink(FORM_LINK_ID, FORM_ID, true);

        ObjectNode formData = JsonNodeFactory.instance.objectNode();
        formData.put("street", "value");

        mockSubmissionData(formLink, formData);
        mockDocument();
        String tokenForTask = getForgedTaskToken(PROCESS_DEFINITION_KEY, FORM_LINK_ID);

        mockMvc.perform(
            post("/api/public/form-association/form-definition/submission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", tokenForTask)
                .content(formData.toString())
        )
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    private void mockSubmissionData(BpmnElementFormIdLink formLink, ObjectNode formData) throws IOException {
        CamundaFormAssociation formAssociation = spy(processFormAssociation(UUID.randomUUID(), UUID.randomUUID()).getFormAssociations().iterator().next());
        when(formAssociation.getFormLink()).thenReturn(formLink);

        when(formAssociationService.getFormAssociationByFormLinkId(any(), any())).thenReturn(Optional.of(formAssociation));

        var formDefinition = formDefinitionOf("public-form-example");
        when(formDefinitionService.getFormDefinitionById(any())).thenReturn(Optional.of(formDefinition));

        final var jsonDocumentContent = JsonDocumentContent.build(formData);
        final Optional<JsonSchemaDocument> document = Optional.of(createDocument(jsonDocumentContent));

        doReturn(document).when(documentService).findBy(any());
        when(processDocumentService.modifyDocumentAndCompleteTask(any())).thenReturn(new ModifyDocumentAndCompleteTaskResultSucceeded(document.orElseThrow()));
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
        final var jsonSchema = JsonSchema.fromResource(jsonSchemaDocumentDefinitionId.path());
        final var definition = new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
        var content = new JsonDocumentContent("{\"street\": \"a Street\"}");
        final JsonSchemaDocument.CreateDocumentResultImpl result = JsonSchemaDocument.create(
            definition,
            content,
            "test@test.com",
            documentSequenceGeneratorService,
            null
        );

        var document = result.resultingDocument().orElseThrow();
        doReturn(Optional.of(document)).when(documentService).findBy(any());
        when(processDocumentService.dispatch(any()))
            .thenReturn(new ModifyDocumentAndCompleteTaskResultSucceeded(document));
    }


}
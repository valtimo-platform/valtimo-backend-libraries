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

package com.ritense.formlink.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation;
import com.ritense.formlink.domain.request.FormLinkRequest;
import com.ritense.formlink.repository.impl.JdbcProcessFormAssociationRepository;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import com.ritense.valtimo.service.CamundaProcessService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CamundaFormAssociationServiceTest extends BaseTest {

    private CamundaFormAssociationService camundaFormAssociationService;
    private FormIoFormDefinitionService formDefinitionService;
    private JsonSchemaDocumentService documentService;
    private ProcessDocumentAssociationService processDocumentAssociationService;
    private CamundaProcessService camundaProcessService;
    private JdbcProcessFormAssociationRepository processFormAssociationRepository;
    private TaskService taskService;
    private FormIoJsonPatchSubmissionTransformerService submissionTransformerService;
    private CamundaProcessFormAssociation processFormAssociation;
    private UUID processFormAssociationId;
    private UUID formId;
    private FormFieldDataResolver formFieldDataResolver;

    @BeforeEach
    public void setUp() {
        processFormAssociationRepository = mock(JdbcProcessFormAssociationRepository.class);
        formDefinitionService = mock(FormIoFormDefinitionService.class);
        documentService = mock(JsonSchemaDocumentService.class);
        processDocumentAssociationService = mock(ProcessDocumentAssociationService.class);
        camundaProcessService = mock(CamundaProcessService.class);
        taskService = mock(TaskService.class);
        submissionTransformerService = mock(FormIoJsonPatchSubmissionTransformerService.class);
        formFieldDataResolver = mock(FormFieldDataResolver.class);

        camundaFormAssociationService = spy(new CamundaFormAssociationService(
            formDefinitionService,
            processFormAssociationRepository,
            documentService,
            processDocumentAssociationService,
            camundaProcessService,
            taskService,
            submissionTransformerService,
            List.of(formFieldDataResolver)
        ));

        processFormAssociationId = UUID.randomUUID();
        formId = UUID.randomUUID();
        processFormAssociation = processFormAssociation(processFormAssociationId, formId);
        when(processFormAssociationRepository.findByCamundaFormAssociationId(any()))
            .thenReturn(processFormAssociation.getFormAssociations().stream().findFirst().orElseThrow());

        mockSpringContextHelper();
    }

    @Test
    public void shouldGetFormAssociationById() {
        final var formAssociationId = processFormAssociation
            .getFormAssociations()
            .stream()
            .findFirst()
            .orElseThrow()
            .getId();

        final var formAssociation = camundaFormAssociationService
            .getFormAssociationById(PROCESS_DEFINITION_KEY, formAssociationId);
        assertThat(formAssociation).isPresent();
    }

    @Test
    public void shouldGetFormAssociationByFormLinkId() {
        final String formLinkId = processFormAssociation.getFormAssociations().stream().findFirst().orElseThrow().getFormLink().getId();

        when(
            processFormAssociationRepository.findByFormLinkId(
                eq(PROCESS_DEFINITION_KEY),
                eq(formLinkId)
            )
        ).thenReturn(processFormAssociation.getFormAssociations().stream().findFirst().orElseThrow());

        final var formAssociation = camundaFormAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, formLinkId);

        assertThat(formAssociation).isPresent();
    }

    @Test
    public void shouldCreateFormAssociation() {
        when(formDefinitionService.formDefinitionExistsById(any())).thenReturn(true);

        final var formId = UUID.randomUUID();
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formId);

        final var formAssociation = camundaFormAssociationService.createFormAssociation(createFormAssociationRequest);

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldModifyFormAssociation() {
        when(formDefinitionService.formDefinitionExistsById(any())).thenReturn(true);

        final var formId = UUID.randomUUID();
        final var formAssociationId = processFormAssociation
            .getFormAssociations().stream().findFirst().orElseThrow().getId();
        final var modifyFormAssociationRequest = modifyFormAssociationRequest(formAssociationId, formId, true);

        final var formAssociation = camundaFormAssociationService.modifyFormAssociation(modifyFormAssociationRequest);

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(modifyFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(modifyFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldUpsertFormAssociation() {
        when(formDefinitionService.formDefinitionExistsById(any())).thenReturn(true);

        final var processDefinitionKey = "aName";
        final var formId = UUID.randomUUID();
        final var formLinkRequest = new FormLinkRequest(
            "id",
            FormAssociationType.USER_TASK,
            formId,
            null,
            null,
            null
        );

        final var formAssociationCreated = camundaFormAssociationService.upsertFormAssociation(
            processDefinitionKey,
            formLinkRequest
        );
        assertThat(formAssociationCreated).isNotNull();
        assertThat(formAssociationCreated).isInstanceOfAny(UserTaskFormAssociation.class);
        assertThat(formAssociationCreated.getId()).isNotNull();
        assertThat(formAssociationCreated.getFormLink().getId()).isEqualTo(formLinkRequest.getId());
        assertThat(formAssociationCreated.getFormLink().getFormId()).isEqualTo(formLinkRequest.getFormId());

        // Now do the update flow

        //Mock the return so the update flow will run
        when(processFormAssociationRepository.findByFormLinkId(any(), any()))
            .thenReturn(processFormAssociation.getFormAssociations().stream().findFirst().orElseThrow());

        final var formAssociationUpdated = camundaFormAssociationService.upsertFormAssociation(
            processDefinitionKey,
            new FormLinkRequest(
                "id2",
                FormAssociationType.START_EVENT,
                UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8"),
                null,
                null,
                null
            )
        );
        assertThat(formAssociationUpdated).isNotNull();
        assertThat(formAssociationUpdated).isInstanceOfAny(StartEventFormAssociation.class);
        assertThat(formAssociationUpdated.getFormLink().getId()).isEqualTo("id2");
        assertThat(formAssociationUpdated.getFormLink().getFormId()).isEqualTo(UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8"));
    }

    @Test
    public void shouldGetPreFilledFormDefinitionByFormLinkId() throws IOException {
        // given
        when(formDefinitionService.formDefinitionExistsById(any())).thenReturn(true);
        var formDefinition = formDefinitionOf("user-task-with-external-form-field");
        when(formDefinitionService.getFormDefinitionById(any())).thenReturn(Optional.of(formDefinition));

        var documentContent = documentContent();
        final var jsonDocumentContent = JsonDocumentContent.build(documentContent);
        Optional<JsonSchemaDocument> documentOptional = Optional.of(createDocument(jsonDocumentContent));
        when(documentService.findBy(any())).thenReturn(documentOptional);

        Map<String, Object> externalFieldVars = new HashMap<>();
        externalFieldVars.put("voornaam", "Jan (OpenZaak)");
        externalFieldVars.put("achternaam", "Test (OpenZaak)");

        when(formFieldDataResolver.supports(anyString())).thenReturn(true);
        when(formFieldDataResolver.get(any(), any(), any(), any())).thenReturn(externalFieldVars);

        final var formLink = processFormAssociation
            .getFormAssociations().stream().findFirst().orElseThrow().getFormLink();
        final String formLinkId = formLink.getId();

        //when
        when(
            processFormAssociationRepository.findByFormLinkId(
                eq(PROCESS_DEFINITION_KEY),
                eq(formLinkId)
            )
        ).thenReturn(processFormAssociation.getFormAssociations().stream().findFirst().orElseThrow());


        final var form = camundaFormAssociationService.getPreFilledFormDefinitionByFormLinkId(
            JsonSchemaDocumentId.existingId(UUID.randomUUID()), PROCESS_DEFINITION_KEY, formLink.getId()
        );

        //then
        assertThat(form).isPresent();
        assertThat(form.get().at("/components/0/defaultValue").textValue()).isEqualTo("Jan"); // Exists already
        assertThat(form.get().at("/components/1/defaultValue").textValue()).isEqualTo("Test (OpenZaak)");
        assertThat(form.get().at("/components/2/defaultValue").textValue()).isEqualTo("Jan (OpenZaak)");
    }

    @Test
    void shouldGetFormDefinitionByFormKey() throws IOException {
        final var formDefinition = formDefinitionOf("user-task-with-external-form-field");
        when(formDefinitionService.getFormDefinitionByName(any())).thenReturn(Optional.of(formDefinition));

        Optional<JsonNode> form = camundaFormAssociationService
            .getPreFilledFormDefinitionByFormKey("form-example", Optional.empty());

        assertThat(form).isPresent();
    }

    @Test
    void shouldGetPrefilledFormDefinitionByFormKey() throws IOException {
        final var documentId = (Document.Id) JsonSchemaDocumentId.existingId(UUID.randomUUID());
        final var formDefinition = formDefinitionOf("user-task-with-external-form-field");
        when(formDefinitionService.getFormDefinitionByName(any())).thenReturn(Optional.of(formDefinition));

        final var documentContent = documentContent();
        final var jsonDocumentContent = JsonDocumentContent.build(documentContent);
        Optional<JsonSchemaDocument> documentOptional = Optional.of(createDocument(jsonDocumentContent));

        Map<String, Object> formFieldData = new HashMap<>();
        formFieldData.put("voornaam", "test-value");
        formFieldData.put("achternaam", "test-value-2");

        when(formFieldDataResolver.supports(eq("oz"))).thenReturn(true);
        when(formFieldDataResolver.get(any(), any(), any(), any())).thenReturn(formFieldData);
        when(documentService.findBy(any())).thenReturn(documentOptional);

        Optional<JsonNode> form = camundaFormAssociationService
            .getPreFilledFormDefinitionByFormKey("form-example", Optional.of(documentId));

        assertThat(form).isPresent();
        assertThat(findArrayEntry(form.get().get("components"), "key", "voornaam")
            .get("defaultValue").textValue()).isEqualTo("Jan");
        assertThat(findArrayEntry(form.get().get("components"), "key", "oz.voornaam")
            .get("defaultValue").textValue()).isEqualTo("test-value");
        assertThat(findArrayEntry(form.get().get("components"), "key", "oz:achternaam")
            .get("defaultValue").textValue()).isEqualTo("test-value-2");
    }

    private ObjectNode documentContent() {
        ObjectNode content = JsonNodeFactory.instance.objectNode();
        content.put("voornaam", "Jan");
        return content;
    }

    private JsonNode findArrayEntry(JsonNode array, String nodeElementName, String nodeElementValue) {
        Iterator<JsonNode> children = array.elements();
        while (children.hasNext()) {
            JsonNode node = children.next();
            if (node.get(nodeElementName).textValue().equals(nodeElementValue)) {
                return node;
            }
        }
        throw new RuntimeException("element not found");
    }

}

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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation;
import com.ritense.formlink.repository.ProcessFormAssociationRepository;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.task.publictask.PublicTaskTokenService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CamundaFormAssociationServiceTest extends BaseTest {

    private CamundaFormAssociationService camundaFormAssociationService;
    private FormIoFormDefinitionService formDefinitionService;
    private JsonSchemaDocumentService documentService;
    private ProcessDocumentAssociationService processDocumentAssociationService;
    private CamundaProcessService camundaProcessService;
    private ProcessFormAssociationRepository processFormAssociationRepository;
    private PublicTaskTokenService publicTaskTokenService;
    private TaskService taskService;
    private FormIoJsonPatchSubmissionTransformerService submissionTransformerService;
    private CamundaProcessFormAssociation processFormAssociation;
    private UUID processFormAssociationId;
    private UUID formId;
    private FormFieldDataResolver formFieldDataResolver;

    @BeforeEach
    public void setUp() {
        processFormAssociationRepository = mock(ProcessFormAssociationRepository.class);
        formDefinitionService = mock(FormIoFormDefinitionService.class);
        documentService = mock(JsonSchemaDocumentService.class);
        processDocumentAssociationService = mock(ProcessDocumentAssociationService.class);
        camundaProcessService = mock(CamundaProcessService.class);
        publicTaskTokenService = mock(PublicTaskTokenService.class);
        taskService = mock(TaskService.class);
        submissionTransformerService = mock(FormIoJsonPatchSubmissionTransformerService.class);
        formFieldDataResolver = mock(FormFieldDataResolver.class);

        camundaFormAssociationService = new CamundaFormAssociationService(
            formDefinitionService,
            processFormAssociationRepository,
            documentService,
            processDocumentAssociationService,
            camundaProcessService,
            publicTaskTokenService,
            taskService,
            submissionTransformerService,
            List.of(formFieldDataResolver)
        );

        processFormAssociationId = UUID.randomUUID();
        formId = UUID.randomUUID();
        processFormAssociation = processFormAssociation(processFormAssociationId, formId);
        when(processFormAssociationRepository.findByProcessDefinitionKey(eq(PROCESS_DEFINITION_KEY)))
            .thenReturn(Optional.of(processFormAssociation));
    }

    @Test
    public void shouldGetFormAssociationById() {
        final var formAssociationId = processFormAssociation.getFormAssociations().stream().findFirst().orElseThrow().getId();

        final var formAssociation = camundaFormAssociationService
            .getFormAssociationById(PROCESS_DEFINITION_KEY, formAssociationId);
        assertThat(formAssociation).isPresent();
    }

    @Test
    public void shouldGetFormAssociationByFormLinkId() {
        final String formLinkId = processFormAssociation.getFormAssociations().stream().findFirst().orElseThrow().getFormLink().getId();

        final var formAssociation = camundaFormAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, formLinkId);

        assertThat(formAssociation).isPresent();
    }

    @Test
    public void shouldCreateFormAssociation() {
        when(formDefinitionService.formDefinitionExistsById(any())).thenReturn(true);
        when(processFormAssociationRepository.saveAndFlush(any())).thenReturn(processFormAssociation);

        final var formId = UUID.randomUUID();
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formId);

        final CamundaFormAssociation formAssociation = camundaFormAssociationService.createFormAssociation(createFormAssociationRequest);

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldModifyFormAssociation() {
        when(formDefinitionService.formDefinitionExistsById(any())).thenReturn(true);
        when(processFormAssociationRepository.saveAndFlush(any())).thenReturn(processFormAssociation);

        final var formId = UUID.randomUUID();
        final var formAssociationId = processFormAssociation
            .getFormAssociations().stream().findFirst().orElseThrow().getId();
        final var modifyFormAssociationRequest = modifyFormAssociationRequest(formAssociationId, formId, true);

        final var formAssociation = camundaFormAssociationService.modifyFormAssociation(modifyFormAssociationRequest);

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(modifyFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(modifyFormAssociationRequest.getFormLinkRequest().getId());
        assertThat(formAssociation.getFormLink().isPublic()).isEqualTo(modifyFormAssociationRequest.getFormLinkRequest().isPublic());
    }

    @Test
    public void shouldGetPreFilledFormDefinitionByFormLinkId() throws IOException {
        //given
        when(formDefinitionService.formDefinitionExistsById(any())).thenReturn(true);
        var formDefinition = formDefinitionOf("user-task-with-external-form-field");
        when(formDefinitionService.getFormDefinitionById(any())).thenReturn(Optional.of(formDefinition));
        when(processFormAssociationRepository.saveAndFlush(any())).thenReturn(processFormAssociation);

        var documentContent = documentContent();
        final var jsonDocumentContent = JsonDocumentContent.build(documentContent);
        Optional<JsonSchemaDocument> documentOptional = Optional.of(createDocument(jsonDocumentContent));
        when(documentService.findBy(any())).thenReturn(documentOptional);

        when(formFieldDataResolver.supports(any())).thenReturn(true);
        when(formFieldDataResolver.get(any(), any(), any())).thenReturn(Map.of("voornaam", "Jan (OpenZaak)"));

        final var formLinkId = processFormAssociation
            .getFormAssociations().stream().findFirst().orElseThrow().getFormLink();

        //when
        final var form = camundaFormAssociationService.getPreFilledFormDefinitionByFormLinkId(
            JsonSchemaDocumentId.existingId(UUID.randomUUID()), PROCESS_DEFINITION_KEY, formLinkId.getId()
        );

        //then
        assertThat(form).isPresent();
        assertThat(form.get().at("/components/0/defaultValue").textValue()).isEqualTo("Jan"); // Exists already
        assertThat(form.get().at("/components/1/defaultValue").textValue()).isEqualTo("Jan (OpenZaak)");
    }

    private ObjectNode documentContent() {
        ObjectNode content = JsonNodeFactory.instance.objectNode();
        content.put("voornaam", "Jan");
        return content;
    }

}
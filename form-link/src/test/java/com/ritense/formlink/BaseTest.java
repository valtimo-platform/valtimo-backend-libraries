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

package com.ritense.formlink;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociationId;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.domain.impl.formassociation.FormAssociations;
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import com.ritense.formlink.domain.request.CreateFormAssociationRequest;
import com.ritense.formlink.domain.request.FormLinkRequest;
import com.ritense.formlink.domain.request.ModifyFormAssociationRequest;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseTest {

    protected static final String PROCESS_DEFINITION_KEY = "process-definition-key";
    protected static final String USERNAME = "test@test.com";
    protected DocumentSequenceGeneratorService documentSequenceGeneratorService;

    public BaseTest() {
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);
    }

    protected CamundaProcessFormAssociation processFormAssociation(UUID id, UUID formId) {
        return new CamundaProcessFormAssociation(
            CamundaProcessFormAssociationId.newId(id),
            PROCESS_DEFINITION_KEY,
            formAssociations(formId)
        );
    }

    protected CamundaProcessFormAssociation processFormAssociation(UUID id, UUID formAssociationsId, UUID formId) {
        return new CamundaProcessFormAssociation(
            CamundaProcessFormAssociationId.newId(id),
            PROCESS_DEFINITION_KEY,
            formAssociations(formAssociationsId, formId)
        );
    }

    private FormAssociations formAssociations(UUID formId) {
        final var formAssociations = new FormAssociations();
        formAssociations.add(
            new UserTaskFormAssociation(
                UUID.randomUUID(),
                new BpmnElementFormIdLink("user-task-id", formId, false)
            )
        );
        return formAssociations;
    }

    private FormAssociations formAssociations(UUID formAssociationsId, UUID formId) {
        final var formAssociations = new FormAssociations();
        formAssociations.add(
            new UserTaskFormAssociation(
                formAssociationsId,
                new BpmnElementFormIdLink("user-task-id", formId, false)
            )
        );
        return formAssociations;
    }

    protected CreateFormAssociationRequest createUserTaskFormAssociationRequest(UUID formId) {
        return new CreateFormAssociationRequest(
            PROCESS_DEFINITION_KEY,
            new FormLinkRequest(
                "userTaskId",
                FormAssociationType.USER_TASK,
                formId,
                null,
                null,
                false
            )
        );
    }

    protected CreateFormAssociationRequest createFormAssociationRequestWithStartEvent(UUID formId) {
        return new CreateFormAssociationRequest(
            PROCESS_DEFINITION_KEY,
            new FormLinkRequest(
                "startEventId",
                FormAssociationType.START_EVENT,
                formId,
                null,
                null,
                false
            )
        );
    }

    protected ModifyFormAssociationRequest modifyFormAssociationRequest(UUID formAssociationId, UUID formId, boolean isPublic) {
        return new ModifyFormAssociationRequest(
            PROCESS_DEFINITION_KEY,
            formAssociationId,
            new FormLinkRequest(
                "userTaskId",
                FormAssociationType.USER_TASK,
                formId,
                null,
                null,
                isPublic
            )
        );
    }

    protected CreateFormDefinitionRequest createFormDefinitionRequest() throws IOException {
        return new CreateFormDefinitionRequest("myForm", rawFormDefinition("form-example"), false);
    }

    protected String rawFormDefinition(String formDefinitionId) throws IOException {
        return IOUtils.toString(
            Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("config/form/" + formDefinitionId + ".json")),
            StandardCharsets.UTF_8
        );
    }

    protected FormIoFormDefinition formDefinitionOf(String formDefinitionId) throws IOException {
        final String formDefinition = rawFormDefinition(formDefinitionId);
        return new FormIoFormDefinition(UUID.randomUUID(), "form-example", formDefinition, false);
    }

    protected Throwable getRootCause(Throwable throwable) {
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    protected JsonSchemaDocumentDefinition definition() {
        final var jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("house");
        final var jsonSchema = JsonSchema.fromResource(jsonSchemaDocumentDefinitionId.path());
        return new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
    }

    protected JsonSchemaDocument createDocument(JsonDocumentContent content) {
        return JsonSchemaDocument
            .create(definition(), content, USERNAME, documentSequenceGeneratorService, null)
            .resultingDocument()
            .orElseThrow();
    }

    protected CamundaProcessJsonSchemaDocumentDefinition processDocumentDefinition(String documentDefinitionName) {
        return new CamundaProcessJsonSchemaDocumentDefinition(
            CamundaProcessJsonSchemaDocumentDefinitionId.newId(
                new CamundaProcessDefinitionKey(PROCESS_DEFINITION_KEY),
                JsonSchemaDocumentDefinitionId.existingId(documentDefinitionName, 1)
            ),
            false
        );
    }

    protected String getForgedTaskToken(String processDefinitionKey, String formLinkId) {
        final Claims claims = new DefaultClaims();

        claims.setIssuer("ValtimoPublicTask");

        claims.setIssuedAt(new Date());
        claims.put("process_definition_key", processDefinitionKey);
        claims.put("FORM_LINK_ID", formLinkId);

        String key = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";

        return Jwts.builder()
            .addClaims(claims)
            .signWith(SignatureAlgorithm.HS512, key.getBytes(StandardCharsets.UTF_8))
            .compact();
    }

}

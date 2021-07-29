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
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.impl.submission.FormIoSubmission;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.FormAssociationSubmissionService;
import com.ritense.formlink.service.SubmissionTransformerService;
import com.ritense.formlink.service.impl.result.FormSubmissionResultFailed;
import com.ritense.formlink.service.result.FormSubmissionResult;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.valtimo.contract.result.OperationError;
import com.ritense.valtimo.service.CamundaTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import javax.transaction.Transactional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CamundaFormAssociationSubmissionService implements FormAssociationSubmissionService {

    private final FormDefinitionService formDefinitionService;
    private final DocumentService documentService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final FormAssociationService formAssociationService;
    private final ProcessDocumentService processDocumentService;
    private final CamundaTaskService camundaTaskService;
    private final SubmissionTransformerService submissionTransformerService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public FormSubmissionResult handleSubmission(
        String processDefinitionKey,
        String formLinkId,
        String documentId,
        String taskInstanceId,
        JsonNode formData
    ) {
        try {
            final FormAssociation formAssociation = formAssociationService
                .getFormAssociationByFormLinkId(processDefinitionKey, formLinkId)
                .orElseThrow();

            final FormIoFormDefinition formDefinition = (FormIoFormDefinition) formDefinitionService
                .getFormDefinitionById(formAssociation.getFormLink().getFormId())
                .orElseThrow();

            JsonSchemaDocument document = null;
            if (documentId != null) {
                document = (JsonSchemaDocument) documentService.findBy(
                    JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
                ).orElseThrow();
            }

            ProcessDocumentDefinition processDocumentDefinition;
            if (document == null) {
                processDocumentDefinition = processDocumentAssociationService
                    .findProcessDocumentDefinition(new CamundaProcessDefinitionKey(processDefinitionKey))
                    .orElseThrow();
            } else {
                processDocumentDefinition = processDocumentAssociationService
                    .findProcessDocumentDefinition(new CamundaProcessDefinitionKey(processDefinitionKey), document.definitionId().version())
                    .orElseThrow();
            }

            var submission = new FormIoSubmission(
                formAssociation,
                formDefinition,
                processDocumentDefinition,
                formData,
                document,
                taskInstanceId,
                processDocumentService,
                camundaTaskService,
                submissionTransformerService,
                applicationEventPublisher
            );
            return submission.apply();
        } catch (RuntimeException ex) {
            return new FormSubmissionResultFailed(parseAndLogException(ex));
        }
    }

    private OperationError parseAndLogException(Exception ex) {
        final UUID referenceId = UUID.randomUUID();
        logger.error("Unexpected error occurred - {}", referenceId, ex);
        return new OperationError.FromString("Unexpected error occurred, please contact support - referenceId: " + referenceId);
    }
}
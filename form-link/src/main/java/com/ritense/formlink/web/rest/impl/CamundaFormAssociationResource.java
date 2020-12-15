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

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.FormAssociationSubmissionService;
import com.ritense.formlink.service.result.FormSubmissionResult;
import com.ritense.formlink.web.rest.FormAssociationResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/form-association", produces = MediaType.APPLICATION_JSON_VALUE)
public class CamundaFormAssociationResource implements FormAssociationResource {

    private final FormAssociationService formAssociationService;
    private final FormAssociationSubmissionService formAssociationSubmissionService;

    public CamundaFormAssociationResource(
        FormAssociationService formAssociationService,
        FormAssociationSubmissionService formAssociationSubmissionService
    ) {
        this.formAssociationService = formAssociationService;
        this.formAssociationSubmissionService = formAssociationSubmissionService;
    }

    @Override
    @GetMapping(value = "/form-definition", params = {"processDefinitionKey", "formLinkId"})
    public ResponseEntity<JsonNode> getFormDefinitionByFormLinkId(
        @RequestParam String processDefinitionKey,
        @RequestParam String formLinkId
    ) {
        return formAssociationService.getFormDefinitionByFormLinkId(processDefinitionKey, formLinkId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping(value = "/form-definition", params = {"documentId", "processDefinitionKey", "formLinkId"})
    public ResponseEntity<JsonNode> getPreFilledFormDefinitionByFormLinkId(
        @RequestParam UUID documentId,
        @RequestParam String processDefinitionKey,
        @RequestParam String formLinkId,
        @RequestParam(required = false) Optional<String> taskInstanceId
    ) {
        return formAssociationService.getPreFilledFormDefinitionByFormLinkId(
            JsonSchemaDocumentId.existingId(documentId),
            processDefinitionKey,
            formLinkId,
            taskInstanceId.orElse(null)
        ).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping(value = "/form-definition", params = {"processDefinitionKey"})
    public ResponseEntity<JsonNode> getStartEventFormDefinitionByProcessDefinitionKey(
        @RequestParam String processDefinitionKey
    ) {
        return formAssociationService.getStartEventFormDefinition(processDefinitionKey)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(value = "/form-definition/submission")
    public ResponseEntity<FormSubmissionResult> handleSubmission(
        @RequestParam String processDefinitionKey,
        @RequestParam String formLinkId,
        @RequestParam(required = false) Optional<String> documentId,
        @RequestParam(required = false) Optional<String> taskInstanceId,
        @RequestBody JsonNode submission
    ) {
        return applyResult(
            formAssociationSubmissionService.handleSubmission(
                processDefinitionKey,
                formLinkId,
                documentId.orElse(null),
                taskInstanceId.orElse(null),
                submission
            )
        );
    }

    <T extends FormSubmissionResult> ResponseEntity<T> applyResult(T result) {
        var httpStatus = result.errors().isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }

}
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
import com.ritense.formlink.web.rest.PublicFormAssociationResource;
import com.ritense.valtimo.task.publictask.PublicTaskTokenParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/public/form-association", produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicCamundaFormAssociationResource implements PublicFormAssociationResource {

    private final FormAssociationService formAssociationService;
    private final FormAssociationSubmissionService formAssociationSubmissionService;

    public PublicCamundaFormAssociationResource(
        FormAssociationService formAssociationService,
        FormAssociationSubmissionService formAssociationSubmissionService
    ) {
        this.formAssociationService = formAssociationService;
        this.formAssociationSubmissionService = formAssociationSubmissionService;
    }

    @Override
    @GetMapping(value = "/form-definition", headers = {"Authorization"})
    public ResponseEntity<JsonNode> getFormDefinitionByFormLinkId(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            return formAssociationService.getPublicFormDefinitionByAuthorization(authorizationHeader)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (PublicTaskTokenParseException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Override
    @GetMapping(value = "/form-definition", params = {"processDefinitionKey"})
    public ResponseEntity<JsonNode> getStartEventFormDefinitionByProcessDefinitionKey(
        @RequestParam String processDefinitionKey
    ) {
        return formAssociationService.getPublicStartEventFormDefinition(processDefinitionKey)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping(value = "/form-definition", headers = {"Authorization"}, params = {"documentId"})
    public ResponseEntity<JsonNode> getPreFilledFormDefinitionByFormLinkId(
        @RequestHeader("Authorization") String authorizationHeaderValue,
        @RequestParam UUID documentId
    ) {
        try {
            return formAssociationService.getPreFilledPublicFormDefinitionByFormLinkId(JsonSchemaDocumentId.newId(documentId), authorizationHeaderValue)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (PublicTaskTokenParseException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Override
    @PostMapping(value = "/form-definition/submission", params = {"processDefinitionKey"})
    public ResponseEntity<FormSubmissionResult> handleStartFormSubmission(
        @RequestParam String processDefinitionKey,
        @RequestBody JsonNode submission
    ) {
        try {
            return applyResult(
                formAssociationSubmissionService.handlePublicStartFormSubmission(
                    processDefinitionKey,
                    submission
                )
            );
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Override
    @PostMapping(value = "/form-definition/submission", headers = {"Authorization"})
    public ResponseEntity<FormSubmissionResult> handleSubmission(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestBody JsonNode submission
    ) {
        try {
            return applyResult(
                formAssociationSubmissionService.handlePublicTaskFormSubmission(
                    authorizationHeader,
                    submission
                )
            );
        } catch (PublicTaskTokenParseException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    <T extends FormSubmissionResult> ResponseEntity<T> applyResult(T result) {
        var httpStatus = result.errors().isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }

}
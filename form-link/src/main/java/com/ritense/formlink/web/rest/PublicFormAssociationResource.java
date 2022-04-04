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

package com.ritense.formlink.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.formlink.service.result.FormSubmissionResult;
import org.springframework.http.ResponseEntity;
import java.util.UUID;

public interface PublicFormAssociationResource {

    ResponseEntity<JsonNode> getFormDefinitionByFormLinkId(String authorizationHeader);

    ResponseEntity<JsonNode> getStartEventFormDefinitionByProcessDefinitionKey(String processDefinitionKey);

    ResponseEntity<JsonNode> getPreFilledFormDefinitionByFormLinkId(String authorizationHeader, UUID documentId);

    ResponseEntity<FormSubmissionResult> handleStartFormSubmission(String processDefinitionKey, JsonNode submission);

    ResponseEntity<FormSubmissionResult> handleSubmission(
        String authorizationHeader,
        JsonNode submission
    );

}
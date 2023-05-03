/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface FormAssociationResource {

    ResponseEntity<JsonNode> getPreFilledFormDefinition(
        String processDefinitionKey,
        String formLinkId,
        Optional<UUID> documentId,
        Optional<String> taskInstanceId
    );

    ResponseEntity<JsonNode> getStartEventFormDefinitionByProcessDefinitionKey(
        String processDefinitionKey,
        Optional<UUID> documentId
    );

    ResponseEntity<JsonNode> getFormDefinitionByFormKey(String formKey, Optional<UUID> documentId);

    ResponseEntity<FormSubmissionResult> handleSubmission(
        String processDefinitionKey,
        String formLinkId,
        Optional<String> documentId,
        Optional<String> taskInstanceId,
        JsonNode submission
    );

}
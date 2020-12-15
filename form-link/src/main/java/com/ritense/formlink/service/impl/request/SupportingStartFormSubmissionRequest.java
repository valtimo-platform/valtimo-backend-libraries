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

package com.ritense.formlink.service.impl.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.formlink.service.result.SubmissionRequest;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class SupportingStartFormSubmissionRequest implements SubmissionRequest {

    private final String processDefinitionKey;
    private final String formLinkId;
    private final String documentId;
    private final JsonNode formData;

    public SupportingStartFormSubmissionRequest(
        String processDefinitionKey,
        String formLinkId,
        String documentId,
        JsonNode formData
    ) {
        assertArgumentNotNull(processDefinitionKey, "processDefinitionKey is required");
        assertArgumentNotNull(formLinkId, "formLinkId is required");
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotNull(formData, "formData is required");
        this.processDefinitionKey = processDefinitionKey;
        this.formLinkId = formLinkId;
        this.documentId = documentId;
        this.formData = formData;
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public String getFormLinkId() {
        return formLinkId;
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Override
    public String getTaskInstanceId() {
        return null;
    }

    @Override
    public JsonNode getFormData() {
        return formData;
    }
}

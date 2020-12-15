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

package com.ritense.processdocument.domain.impl.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.processdocument.domain.request.Request;

import javax.validation.constraints.NotNull;

public class ProcessDocumentDefinitionRequest implements Request {

    @JsonProperty
    private String processDefinitionKey;

    @JsonProperty
    private String documentDefinitionName;

    @JsonProperty("canInitializeDocument")
    private boolean canInitializeDocument;

    @JsonCreator
    public ProcessDocumentDefinitionRequest(
        @JsonProperty(value = "processDefinitionKey", required = true) @NotNull String processDefinitionKey,
        @JsonProperty(value = "documentDefinitionName", required = true) @NotNull String documentDefinitionName,
        @JsonProperty(value = "canInitializeDocument", required = true) boolean canInitializeDocument
    ) {
        this.processDefinitionKey = processDefinitionKey;
        this.documentDefinitionName = documentDefinitionName;
        this.canInitializeDocument = canInitializeDocument;
    }

    public String processDefinitionKey() {
        return processDefinitionKey;
    }

    public String documentDefinitionName() {
        return documentDefinitionName;
    }

    public boolean canInitializeDocument() {
        return canInitializeDocument;
    }

}
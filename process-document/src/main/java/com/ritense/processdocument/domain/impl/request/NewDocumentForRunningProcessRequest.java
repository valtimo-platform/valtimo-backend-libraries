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
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.processdocument.domain.request.Request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class NewDocumentForRunningProcessRequest implements Request {

    @JsonProperty
    private final String processDefinitionKey;

    @JsonProperty
    private final String processInstanceId;

    @JsonProperty("request")
    private final NewDocumentRequest newDocumentRequest;

    @JsonCreator
    public NewDocumentForRunningProcessRequest(
        @JsonProperty(value = "processDefinitionKey", required = true) @NotNull String processDefinitionKey,
        @JsonProperty(value = "processInstanceId", required = true) @NotNull String processInstanceId,
        @JsonProperty(value = "request", required = true) @NotNull @Valid NewDocumentRequest newDocumentRequest
    ) {
        this.processDefinitionKey = processDefinitionKey;
        this.processInstanceId = processInstanceId;
        this.newDocumentRequest = newDocumentRequest;
    }

    public String processDefinitionKey() {
        return processDefinitionKey;
    }

    public String processInstanceId() {
        return processInstanceId;
    }

    public NewDocumentRequest newDocumentRequest() {
        return newDocumentRequest;
    }

}
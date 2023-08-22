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

package com.ritense.processdocument.domain.impl.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.processdocument.domain.request.Request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class ModifyDocumentAndStartProcessRequest implements Request {

    @JsonProperty
    private final String processDefinitionKey;

    @JsonProperty("request")
    private final ModifyDocumentRequest modifyDocumentRequest;

    @JsonProperty
    private String processInstanceId;

    @JsonIgnore
    private Map<String, Object> processVars;

    @JsonCreator
    public ModifyDocumentAndStartProcessRequest(
        @JsonProperty(value = "processDefinitionKey", required = true) @NotNull String processDefinitionKey,
        @JsonProperty(value = "request", required = true) @NotNull @Valid ModifyDocumentRequest modifyDocumentRequest
    ) {
        this.processDefinitionKey = processDefinitionKey;
        this.modifyDocumentRequest = modifyDocumentRequest;
    }

    public String processDefinitionKey() {
        return processDefinitionKey;
    }

    public ModifyDocumentRequest modifyDocumentRequest() {
        return modifyDocumentRequest;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String processInstanceId() {
        return processInstanceId;
    }

    public ModifyDocumentAndStartProcessRequest withProcessVars(Map<String, Object> processVars) {
        this.processVars = processVars;
        return this;
    }

    public Map<String, Object> getProcessVars() {
        return processVars;
    }

}
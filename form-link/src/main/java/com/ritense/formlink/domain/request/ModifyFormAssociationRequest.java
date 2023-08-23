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

package com.ritense.formlink.domain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@Deprecated(since = "10.6.0", forRemoval = true)
public class ModifyFormAssociationRequest {

    @JsonProperty
    private final String processDefinitionKey;

    @JsonProperty
    private final UUID formAssociationId;

    @JsonProperty
    private final FormLinkRequest formLinkRequest;

    @JsonCreator
    public ModifyFormAssociationRequest(
        @JsonProperty(value = "processDefinitionKey", required = true) String processDefinitionKey,
        @JsonProperty(value = "formAssociationId", required = true) UUID formAssociationId,
        @JsonProperty(value = "formLinkRequest", required = true) FormLinkRequest formLinkRequest
    ) {
        this.processDefinitionKey = processDefinitionKey;
        this.formAssociationId = formAssociationId;
        this.formLinkRequest = formLinkRequest;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public UUID getFormAssociationId() {
        return formAssociationId;
    }

    public FormLinkRequest getFormLinkRequest() {
        return formLinkRequest;
    }
}

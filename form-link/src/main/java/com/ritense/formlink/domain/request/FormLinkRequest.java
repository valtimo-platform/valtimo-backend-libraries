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
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import java.util.UUID;

public class FormLinkRequest {

    @JsonProperty
    private final String id;

    @JsonProperty
    private final FormAssociationType type;

    @JsonProperty
    private String customUrl;

    @JsonProperty
    private String angularStateUrl;

    @JsonProperty
    private UUID formId;

    @JsonProperty
    private String formFlowId;

    @JsonCreator
    public FormLinkRequest(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "type", required = true) FormAssociationType type,
        @JsonProperty(value = "formId") UUID formId,
        @JsonProperty(value = "formFlowId") String formFlowId,
        @JsonProperty(value = "customUrl") String customUrl,
        @JsonProperty(value = "angularStateUrl") String angularStateUrl
    ) {
        this.id = id;
        this.type = type;
        this.formId = formId;
        this.formFlowId = formFlowId;
        this.customUrl = customUrl;
        this.angularStateUrl = angularStateUrl;
    }

    public String getId() {
        return id;
    }

    public FormAssociationType getType() {
        return type;
    }

    public UUID getFormId() {
        return formId;
    }

    public String getFormFlowId() {
        return formFlowId;
    }

    public String getCustomUrl() {
        return customUrl;
    }

    public String getAngularStateUrl() {
        return angularStateUrl;
    }
}
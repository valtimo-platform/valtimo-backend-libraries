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

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentDefinitionProcessRequest {

    private String processDefinitionKey;
    private String linkType;

    public DocumentDefinitionProcessRequest(
        @JsonProperty(value = "processDefinitionKey", required = true) String processDefinitionKey,
        @JsonProperty(value = "linkType") String linkType
    ) {
        this.processDefinitionKey = processDefinitionKey;
        this.linkType = linkType;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getLinkType() {
        return linkType;
    }
}

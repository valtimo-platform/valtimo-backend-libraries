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

import com.ritense.document.domain.Document;
import com.ritense.processdocument.domain.request.Request;

import java.util.Map;
import java.util.Objects;

public class StartProcessForDocumentRequest implements Request {
    private final Document.Id documentId;
    private final String processDefinitionKey;
    private final Map<String, Object> processVars;

    public StartProcessForDocumentRequest(Document.Id documentId, String processDefinitionKey, Map<String, Object> processVars) {
        this.documentId = documentId;
        this.processDefinitionKey = processDefinitionKey;
        this.processVars = processVars;
    }

    public Document.Id getDocumentId() {
        return this.documentId;
    }

    public String getProcessDefinitionKey() {
        return this.processDefinitionKey;
    }

    public Map<String, Object> getProcessVars() {
        return this.processVars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartProcessForDocumentRequest that = (StartProcessForDocumentRequest) o;
        return Objects.equals(getDocumentId(), that.getDocumentId()) && Objects.equals(getProcessDefinitionKey(), that.getProcessDefinitionKey()) && Objects.equals(getProcessVars(), that.getProcessVars());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDocumentId(), getProcessDefinitionKey(), getProcessVars());
    }

    public String toString() {
        return "StartProcessForDocumentRequest(documentId=" + this.getDocumentId() + ", processDefinitionKey=" + this.getProcessDefinitionKey() + ", processVars=" + this.getProcessVars() + ")";
    }
}
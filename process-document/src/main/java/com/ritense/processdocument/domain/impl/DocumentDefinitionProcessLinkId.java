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

package com.ritense.processdocument.domain.impl;

import com.ritense.valtimo.contract.domain.AbstractId;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
public class DocumentDefinitionProcessLinkId
    extends AbstractId<DocumentDefinitionProcessLinkId> {

    @Column(name = "document_definition_name", nullable = false)
    private String documentDefinitionName;

    @Column(name = "process_definition_key", nullable = false)
    private String processDefinitionKey;

    public DocumentDefinitionProcessLinkId() {}

    private DocumentDefinitionProcessLinkId(
        final String documentDefinitionName,
        final String processDefinitionKey) {
        assertArgumentNotNull(documentDefinitionName, "The documentDefinitionName is required");
        assertArgumentNotNull(processDefinitionKey, "The processDefinitionKey is required");
        this.documentDefinitionName = documentDefinitionName;
        this.processDefinitionKey = processDefinitionKey;
    }

    public static DocumentDefinitionProcessLinkId newId(
        String documentDefinitionName,
        String processDefinitionKey
    ) {
        return new DocumentDefinitionProcessLinkId(documentDefinitionName, processDefinitionKey);
    }

    public static DocumentDefinitionProcessLinkId existingId(
        String documentDefinitionName,
        String processDefinitionKey
    ) {
        return new DocumentDefinitionProcessLinkId(documentDefinitionName, processDefinitionKey).newIdentity();
    }

    public String getDocumentDefinitionName() {
        return documentDefinitionName;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentDefinitionProcessLinkId)) {
            return false;
        }
        DocumentDefinitionProcessLinkId that = (DocumentDefinitionProcessLinkId) o;
        return documentDefinitionName.equals(that.documentDefinitionName) &&
            processDefinitionKey.equals(that.processDefinitionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentDefinitionName, processDefinitionKey);
    }
}

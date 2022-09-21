/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
public class DocumentDefinitionProcessLinkId
    extends AbstractId<DocumentDefinitionProcessLinkId> {

    @Column(name = "document_definition_name", nullable = false)
    private String documentDefinitionName;

    @Column(name = "process_id", nullable = false)
    private String processId;

    private DocumentDefinitionProcessLinkId(
        final String documentDefinitionName,
        final String processId) {
        assertArgumentNotNull(documentDefinitionName, "The documentDefinitionName is required");
        assertArgumentNotNull(processId, "The processId is required");
        this.documentDefinitionName = documentDefinitionName;
        this.processId = processId;
    }

    public static DocumentDefinitionProcessLinkId newId(
        String documentDefinitionName,
        String processId
    ) {
        return new DocumentDefinitionProcessLinkId(documentDefinitionName, processId);
    }

    public static DocumentDefinitionProcessLinkId existingId(
        String documentDefinitionName,
        String processId
    ) {
        return new DocumentDefinitionProcessLinkId(documentDefinitionName, processId).newIdentity();
    }

    public String documentDefinitionName() {
        return documentDefinitionName;
    }

    public String processId() {
        return processId;
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
            processId.equals(that.processId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentDefinitionName, processId);
    }
}

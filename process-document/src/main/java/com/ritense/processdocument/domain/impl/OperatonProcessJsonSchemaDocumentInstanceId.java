/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.processdocument.domain.ProcessDocumentInstanceId;
import com.ritense.valtimo.contract.domain.AbstractId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Objects;

@Embeddable
public class OperatonProcessJsonSchemaDocumentInstanceId
    extends AbstractId<OperatonProcessJsonSchemaDocumentInstanceId>
    implements ProcessDocumentInstanceId {

    @Embedded
    private OperatonProcessInstanceId processInstanceId;

    @Embedded
    private JsonSchemaDocumentId documentId;

    private OperatonProcessJsonSchemaDocumentInstanceId(
        OperatonProcessInstanceId processInstanceId,
        JsonSchemaDocumentId documentId
    ) {
        assertArgumentNotNull(processInstanceId, "processInstanceId is required");
        assertArgumentNotNull(documentId, "documentId is required");
        this.processInstanceId = processInstanceId;
        this.documentId = documentId;
    }

    OperatonProcessJsonSchemaDocumentInstanceId() {
    }

    public static OperatonProcessJsonSchemaDocumentInstanceId existingId(
        OperatonProcessInstanceId processInstanceId,
        JsonSchemaDocumentId documentId
    ) {
        return new OperatonProcessJsonSchemaDocumentInstanceId(processInstanceId, documentId);
    }

    public static OperatonProcessJsonSchemaDocumentInstanceId newId(
        OperatonProcessInstanceId processInstanceId,
        JsonSchemaDocumentId documentId
    ) {
        return new OperatonProcessJsonSchemaDocumentInstanceId(processInstanceId, documentId).newIdentity();
    }

    @Override
    public OperatonProcessInstanceId processInstanceId() {
        return processInstanceId;
    }

    @Override
    public JsonSchemaDocumentId documentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperatonProcessJsonSchemaDocumentInstanceId that)) {
            return false;
        }
        return processInstanceId.equals(that.processInstanceId)
            && documentId.equals(that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInstanceId, documentId);
    }

}
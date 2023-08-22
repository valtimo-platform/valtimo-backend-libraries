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

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.ProcessDocumentDefinitionId;
import com.ritense.valtimo.contract.domain.AbstractId;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.util.Objects;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
public class CamundaProcessJsonSchemaDocumentDefinitionId extends AbstractId<CamundaProcessJsonSchemaDocumentDefinitionId>
    implements ProcessDocumentDefinitionId {

    @Embedded
    private CamundaProcessDefinitionKey processDefinitionKey;

    @Embedded
    private JsonSchemaDocumentDefinitionId documentDefinitionId;

    private CamundaProcessJsonSchemaDocumentDefinitionId(
        final CamundaProcessDefinitionKey processDefinitionKey,
        final JsonSchemaDocumentDefinitionId documentDefinitionId
    ) {
        assertArgumentNotNull(processDefinitionKey, "processDefinitionKey is required");
        assertArgumentNotNull(documentDefinitionId, "documentDefinitionId is required");
        this.processDefinitionKey = processDefinitionKey;
        this.documentDefinitionId = documentDefinitionId;
    }

    private CamundaProcessJsonSchemaDocumentDefinitionId() {
    }

    public static CamundaProcessJsonSchemaDocumentDefinitionId existingId(
        CamundaProcessDefinitionKey processDefinitionKey,
        JsonSchemaDocumentDefinitionId documentDefinitionId
    ) {
        return new CamundaProcessJsonSchemaDocumentDefinitionId(processDefinitionKey, documentDefinitionId);
    }

    public static CamundaProcessJsonSchemaDocumentDefinitionId newId(
        CamundaProcessDefinitionKey processDefinitionKey,
        JsonSchemaDocumentDefinitionId documentDefinitionId
    ) {
        return new CamundaProcessJsonSchemaDocumentDefinitionId(processDefinitionKey, documentDefinitionId).newIdentity();
    }

    @Override
    public CamundaProcessDefinitionKey processDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public JsonSchemaDocumentDefinitionId documentDefinitionId() {
        return documentDefinitionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CamundaProcessJsonSchemaDocumentDefinitionId)) {
            return false;
        }
        CamundaProcessJsonSchemaDocumentDefinitionId that = (CamundaProcessJsonSchemaDocumentDefinitionId) o;
        return processDefinitionKey.equals(that.processDefinitionKey) &&
            documentDefinitionId.equals(that.documentDefinitionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processDefinitionKey, documentDefinitionId);
    }

}
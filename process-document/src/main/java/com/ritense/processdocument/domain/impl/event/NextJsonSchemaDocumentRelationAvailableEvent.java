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

package com.ritense.processdocument.domain.impl.event;

import com.ritense.document.domain.relation.DocumentRelationType;
import com.ritense.valtimo.contract.processdocument.event.NextDocumentRelationAvailableEvent;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class NextJsonSchemaDocumentRelationAvailableEvent implements NextDocumentRelationAvailableEvent {

    private final String previousDocumentId;
    private final String relationType;
    private final String nextDocumentId;

    public NextJsonSchemaDocumentRelationAvailableEvent(String previousDocumentId, String nextDocumentId) {
        assertArgumentNotNull(previousDocumentId, "previousDocumentId is required");
        assertArgumentNotNull(nextDocumentId, "nextDocumentId is required");
        this.previousDocumentId = previousDocumentId;
        this.relationType = DocumentRelationType.NEXT.name();
        this.nextDocumentId = nextDocumentId;
    }

    @Override
    public String previousDocumentId() {
        return previousDocumentId;
    }

    @Override
    public String relationType() {
        return relationType;
    }

    @Override
    public String nextDocumentId() {
        return nextDocumentId;
    }

}
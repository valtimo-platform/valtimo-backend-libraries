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

package com.ritense.document.domain.impl.listener;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation;
import com.ritense.document.domain.listener.RelatedDocumentAvailableEventListener;
import com.ritense.document.domain.relation.DocumentRelationType;
import com.ritense.document.service.DocumentService;
import com.ritense.valtimo.contract.processdocument.event.NextDocumentRelationAvailableEvent;

import java.util.UUID;

public class RelatedJsonSchemaDocumentAvailableEventListenerImpl implements RelatedDocumentAvailableEventListener {

    private final DocumentService documentService;

    public RelatedJsonSchemaDocumentAvailableEventListenerImpl(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public void handle(NextDocumentRelationAvailableEvent event) {
        final var documentId = JsonSchemaDocumentId.existingId(UUID.fromString(event.previousDocumentId()));
        final var documentRelation = documentRelation(event);
        documentService.assignDocumentRelation(documentId, documentRelation);
    }

    private JsonSchemaDocumentRelation documentRelation(NextDocumentRelationAvailableEvent event) {
        return new JsonSchemaDocumentRelation(
            JsonSchemaDocumentId.existingId(UUID.fromString(event.nextDocumentId())),
            DocumentRelationType.valueOf(event.relationType())
        );
    }

}
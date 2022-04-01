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

package com.ritense.valtimo.contract.document.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

public class DocumentRelatedFileSubmittedEvent {

    private final UUID documentId;
    private final UUID resourceId;
    private final String documentDefinitionName;

    @JsonCreator
    public DocumentRelatedFileSubmittedEvent(UUID documentId, UUID resourceId, String documentDefinitionName) {
        this.documentId = documentId;
        this.resourceId = resourceId;
        this.documentDefinitionName = documentDefinitionName;
    }

    @JsonIgnore(value = false)
    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getDocumentDefinitionName() {
        return documentDefinitionName;
    }
}

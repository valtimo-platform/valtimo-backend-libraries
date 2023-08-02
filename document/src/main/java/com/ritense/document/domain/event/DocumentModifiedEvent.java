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

package com.ritense.document.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.document.config.DocumentSpringContextHelper;
import com.ritense.document.domain.Document;

import java.util.List;

public interface DocumentModifiedEvent<T extends DocumentFieldChangedEvent> {

    @JsonProperty
    Document.Id documentId();

    List<T> changes();

    @JsonProperty("changes")
    default List<T> registeredChanges() {
        List<T> changes = null;

        Boolean registerDocumentChanges = DocumentSpringContextHelper
            .getProperty("valtimo.audit.auditDocumentChanges", Boolean.class);

        if (registerDocumentChanges != null && registerDocumentChanges) {
            changes = changes();
        }

        return changes;
    }

}

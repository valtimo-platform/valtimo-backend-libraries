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

package com.ritense.document.domain.impl.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent;

public class JsonSchemaDocumentDefinitionDeployedEvent implements DocumentDefinitionDeployedEvent {

    private final DocumentDefinition documentDefinition;

    @JsonCreator
    public JsonSchemaDocumentDefinitionDeployedEvent(DocumentDefinition documentDefinition) {
        this.documentDefinition = documentDefinition;
    }

    @Override
    public DocumentDefinition documentDefinition() {
        return documentDefinition;
    }

}
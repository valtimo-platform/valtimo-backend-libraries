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

package com.ritense.document.exception;

import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;

public class DocumentDefinitionNameMismatchException extends RuntimeException {

    private static final String message = "The supplied document definition id [%s] and schema id [%s] do not match";

    public DocumentDefinitionNameMismatchException(String message) {
        super(message);
    }

    public DocumentDefinitionNameMismatchException(JsonSchemaDocumentDefinitionId id, JsonSchema schema) {
        super(String.format(message, id.toString(), schema.getSchema().getId()));
    }

}
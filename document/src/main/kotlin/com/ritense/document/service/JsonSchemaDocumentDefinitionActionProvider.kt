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
package com.ritense.document.service

import com.ritense.authorization.Action
import com.ritense.authorization.ResourceActionProvider
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition

class JsonSchemaDocumentDefinitionActionProvider : ResourceActionProvider<JsonSchemaDocumentDefinition> {
    override fun getAvailableActions(): List<Action<JsonSchemaDocumentDefinition>> {
        return listOf(VIEW, VIEW_LIST, CREATE, MODIFY, DELETE)
    }

    companion object {
        @JvmField val VIEW = Action<JsonSchemaDocumentDefinition>(Action.VIEW)
        @JvmField val VIEW_LIST = Action<JsonSchemaDocumentDefinition>(Action.VIEW_LIST)
        @JvmField val CREATE = Action<JsonSchemaDocumentDefinition>(Action.CREATE)
        @JvmField val MODIFY = Action<JsonSchemaDocumentDefinition>(Action.MODIFY)
        @JvmField val DELETE = Action<JsonSchemaDocumentDefinition>(Action.DELETE)
    }
}

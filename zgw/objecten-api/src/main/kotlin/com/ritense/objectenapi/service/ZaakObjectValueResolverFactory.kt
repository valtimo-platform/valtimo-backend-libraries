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

package com.ritense.objectenapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.valueresolver.ValueResolverFactory
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.UUID
import java.util.function.Function

class ZaakObjectValueResolverFactory(
    val zaakObjectService: ZaakObjectService,
    val objectMapper: ObjectMapper
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return ZaakObjectConstants.ZAAKOBJECT_PREFIX
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        TODO()
    }

    override fun createResolver(documentInstanceId: String): Function<String, Any?> {
        return Function { requestedValue ->
            val requestValue = ZaakObjectDataResolver.RequestedData(requestedValue)
            val zaakObject = zaakObjectService.getZaakObjectOfTypeByName(UUID.fromString(documentInstanceId), requestValue.objectType)
            val dataAsJsonNode = objectMapper.valueToTree<JsonNode>(zaakObject.record.data)
            dataAsJsonNode.at(requestValue.path)
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any>
    ) {
        TODO()
    }
}

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
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valueresolver.ValueResolverFactory
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.UUID
import java.util.function.Function

class ZaakObjectValueResolverFactory(
    val zaakObjectService: ZaakObjectService,
    val objectMapper: ObjectMapper,
    val processDocumentService: ProcessDocumentService,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return ZaakObjectConstants.ZAAKOBJECT_PREFIX
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        return Function { requestedValue ->
            val documentId = processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), variableScope).toString()
            getZaakData(requestedValue, documentId)
        }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return Function { requestedValue ->
            getZaakData(requestedValue, documentId)
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any>
    ) {
        TODO()
    }

    private fun getZaakData(requestedValue: String, documentId: String): Any? {
        val requestedData = ZaakObjectDataResolver.RequestedData(requestedValue)
        val zaakObject = zaakObjectService.getZaakObjectOfTypeByName(UUID.fromString(documentId), requestedData.objectType)
        val dataAsJsonNode = objectMapper.valueToTree<JsonNode>(zaakObject.record.data)
        val node = dataAsJsonNode.at(requestedData.path)
        return if (node == null || node.isMissingNode || node.isNull) {
            null
        } else if (node.isValueNode || node.isArray || node.isObject) {
            Mapper.INSTANCE.get().treeToValue(node, Object::class.java)
        } else {
            node.asText()
        }
    }
}

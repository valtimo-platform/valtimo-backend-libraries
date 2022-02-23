/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.taak.resolve

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.function.Function

/**
 * This resolver can resolve requestedValues against Document linked to the process
 *
 * The value of the requestedValue should be in the format doc:/some/json/pointer
 */
class DocumentValueResolverFactory(
    private val processDocumentService: ProcessDocumentService,
    private val documentService: DocumentService,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return "doc"
    }

    override fun createResolver(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope
    ): Function<String, Any?> {
        val document = processDocumentService.getDocument(processInstanceId, variableScope)

        return Function { requestedValue ->
            val value = document.content().getValueBy(JsonPointer.valueOf(requestedValue)).orElse(null)
            if (value?.isValueNode == true) {
                Mapper.INSTANCE.get().treeToValue(value, Object::class.java)
            } else {
                null
            }
        }
    }

    override fun handleValues(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        values: Map<String, Any>
    ) {
        val document = processDocumentService.getDocument(processInstanceId, variableScope)
        val jsonPatchBuilder = JsonPatchBuilder()

        values.forEach {
            val path = JsonPointer.valueOf(it.key.substringAfter(":"))
            val valueNode = toValueNode(it.value)
            when (it.key.substringBefore(":", missingDelimiterValue = "")) {
                "add" -> jsonPatchBuilder.add(path, valueNode)
                "replace" -> jsonPatchBuilder.replace(path, valueNode)
                else -> throw IllegalArgumentException("Missing 'add' or 'replace' in '${it.key}'")
            }
        }

        val documentContent = document.content().asJson()
        JsonPatchService.apply(jsonPatchBuilder.build(), documentContent)
        documentService.modifyDocument(
            ModifyDocumentRequest(
                document?.id().toString(),
                documentContent,
                document?.version().toString()
            )
        )
    }

    private fun toValueNode(value: Any) : JsonNode {
        return try {
            Mapper.INSTANCE.get().readTree(value.toString())
        } catch (e : JsonParseException) {
            TextNode.valueOf(value.toString())
        }
    }

}
/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.resolver

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jayway.jsonpath.InvalidPathException
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.jayway.jsonpath.internal.path.PathCompiler
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.document.exception.ModifyDocumentException
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import com.ritense.valueresolver.ValueResolverFactory
import com.ritense.valueresolver.exception.ValueResolverValidationException
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.UUID
import java.util.function.Function

/**
 * This resolver can resolve requestedValues against the Document JSON content
 *
 * The value of the requestedValue should be in the format doc:some.json.path
 */
class DocumentJsonValueResolverFactory(
    private val processDocumentService: ProcessDocumentService,
    private val documentService: DocumentService,
    private val documentDefinitionService: JsonSchemaDocumentDefinitionService,
    private val objectMapper: ObjectMapper,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return PREFIX
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        val document = processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), variableScope)
        return createResolver(document)
    }

    override fun createValidator(documentDefinitionName: String): Function<String, Unit> {
        val documentDefinition = documentDefinitionService.findLatestByName(documentDefinitionName)
            .orElseThrow { UnknownDocumentDefinitionException(documentDefinitionName) }

        return Function { requestedValue ->
            if (isJsonPointer(requestedValue)) {
                validateJsonPointer(documentDefinition, requestedValue)
            } else {
                validateJsonPath(documentDefinition, requestedValue)
            }
        }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return createResolver(
            AuthorizationContext.runWithoutAuthorization { documentService.get(documentId) }
        )
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        val document = AuthorizationContext.runWithoutAuthorization {
            processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), variableScope)
        }
        val documentContent = document.content().asJson()
        buildJsonPatch(documentContent, values)

        try {
            //TODO: PBAC MODIFY check
            AuthorizationContext.runWithoutAuthorization {
                documentService.modifyDocument(document, documentContent)
            }
        } catch (exception: ModifyDocumentException) {
            throw RuntimeException(
                "Failed to handle values for processInstance '$processInstanceId'. Values: ${values}.",
                exception
            )
        }
    }

    override fun handleValues(documentId: UUID, values: Map<String, Any?>) {
        val document = AuthorizationContext.runWithoutAuthorization { documentService.get(documentId.toString()) }
        val documentContent = document.content().asJson()
        buildJsonPatch(documentContent, values)

        try {
            AuthorizationContext.runWithoutAuthorization { documentService.modifyDocument(document, documentContent) }
        } catch (exception: ModifyDocumentException) {
            throw RuntimeException(
                "Failed to handle values for document '$documentId'. Values: ${values}.",
                exception
            )
        }
    }

    override fun preProcessValuesForNewCase(values: Map<String, Any?>): ObjectNode {
        val emptyDocumentContent = objectMapper.createObjectNode()
        buildJsonPatch(emptyDocumentContent, values)
        return emptyDocumentContent
    }

    override fun getResolvableKeys(documentDefinitionName: String, version: Long): List<String> {
        val documentDefinition = documentDefinitionService.findByNameAndVersion(documentDefinitionName, version).orElseThrow()
        return documentDefinitionService.getPropertyNames(documentDefinition)
    }

    override fun getResolvableKeys(documentDefinitionName: String): List<String> {
        val documentDefinition = documentDefinitionService.findLatestByName(documentDefinitionName).orElseThrow()
        return documentDefinitionService.getPropertyNames(documentDefinition)
    }

    private fun buildJsonPatch(jsonNode: JsonNode, values: Map<String, Any?>) {
        val jsonPatchBuilder = JsonPatchBuilder()

        values.forEach {
            val pathString = it.key.substringAfter(":")
            val valueNode = toValueNode(it.value)
            //support for the old style of adding items to an array by using /-/ in the path
            if (pathString.contains("/-/")) {
                val arrayPointer = JsonPointer.compile(pathString.substringBefore("/-"))
                val array = jsonNode.at(arrayPointer)
                if (array.isMissingNode) {
                    jsonPatchBuilder.add(arrayPointer, JsonNodeFactory.instance.arrayNode())
                }

                //ensure object exist in array
                val itemPointer =
                    arrayPointer.appendIndex(array.size()) //array.size returns 0 for MissingNode
                val newArrayObject = JsonNodeFactory.instance.objectNode()
                //Add actual item to its position
                val property = pathString.substringAfter("/-")
                buildJsonPatch(newArrayObject, mapOf(property to it.value))
                jsonPatchBuilder.add(itemPointer, newArrayObject)
            } else {
                val path = JsonPointer.valueOf(pathString)
                jsonPatchBuilder.addJsonNodeValue(jsonNode, path, valueNode)
            }
        }

        JsonPatchService.apply(jsonPatchBuilder.build(), jsonNode)
    }

    private fun validateJsonPointer(documentDefinition: JsonSchemaDocumentDefinition, jsonPointer: String) {
        if (!documentDefinition.schema.schema.definesProperty(jsonPointer)) {
            throw ValueResolverValidationException(
                "JsonPointer '$jsonPointer' doesn't point to any property inside document definition '${documentDefinition.id.name()}'"
            )
        }
    }

    private fun validateJsonPath(documentDefinition: JsonSchemaDocumentDefinition, jsonPathPostfix: String) {
        val jsonPath = "$.$jsonPathPostfix"
        try {
            PathCompiler.compile(jsonPath)
        } catch (e: InvalidPathException) {
            throw ValueResolverValidationException(
                "Failed to compile JsonPath '$jsonPath' for document definition '${documentDefinition.id.name()}'",
                e
            )
        }
        if (!documentDefinitionService.isValidJsonPath(documentDefinition, jsonPath)) {
            throw ValueResolverValidationException(
                "JsonPath '$jsonPath' doesn't point to any property inside document definition '${documentDefinition.id.name()}'"
            )
        }
    }

    private fun createResolver(document: Document): Function<String, Any?> {
        return Function { requestedValue ->
            if (isJsonPointer(requestedValue)) {
                resolveForJsonPointer(document, requestedValue)
            } else {
                resolveForJsonPath(document, requestedValue)
            }
        }
    }

    private fun isJsonPointer(path: String) = path.startsWith("/")

    private fun resolveForJsonPointer(document: Document, jsonPointer: String): Any? {
        val node = document.content().getValueBy(JsonPointer.valueOf(jsonPointer)).orElse(null)
        return if (node == null || node.isMissingNode || node.isNull) {
            null
        } else if (node.isValueNode || node.isArray || node.isObject) {
            objectMapper.treeToValue(node, Object::class.java)
        } else {
            node.asText()
        }
    }

    private fun resolveForJsonPath(document: Document, jsonPathPostfix: String): Any? {
        return try {
            JsonPath.read<Any?>(document.content().asJson().toString(), "$.$jsonPathPostfix")
        } catch (ignore: PathNotFoundException) {
            null
        }
    }

    private fun toValueNode(value: Any?): JsonNode {
        return objectMapper.valueToTree(value)
    }

    companion object {
        const val PREFIX = "doc"
    }

}

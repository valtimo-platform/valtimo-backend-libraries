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

package com.ritense.document.service

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode
import com.ritense.document.domain.DocumentMigrationConflict
import com.ritense.document.domain.DocumentMigrationConflictResponse
import com.ritense.document.domain.DocumentMigrationPatch
import com.ritense.document.domain.DocumentMigrationRequest
import com.ritense.document.domain.getJsonPointers
import com.ritense.document.domain.getProperty
import com.ritense.document.domain.getTypeReference
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.patch.JsonPatchFilterFlag
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.document.exception.DocumentMigrationPatchException
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.Companion.byDocumentDefinitionId
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.logging.withLoggingContext
import com.ritense.valtimo.contract.annotation.PublicBean
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import org.everit.json.schema.Schema
import org.springframework.context.ApplicationContext
import org.springframework.context.expression.MapAccessor
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.integration.json.JsonPropertyAccessor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class DocumentMigrationService(
    private val documentDefinitionService: JsonSchemaDocumentDefinitionService,
    private val documentRepository: JsonSchemaDocumentRepository,
    private val applicationContext: ApplicationContext,
    private val objectMapper: ObjectMapper,
) {
    fun getConflicts(migrationRequest: DocumentMigrationRequest): DocumentMigrationConflictResponse {
        val sourceId = migrationRequest.getDocumentDefinitionIdSource()
        return withLoggingContext(JsonSchemaDocumentDefinition::class.java, sourceId.toString()) {
            val targetId = migrationRequest.getDocumentDefinitionIdTarget()
            val sourceSchema = documentDefinitionService.findBy(sourceId).orElseThrow().schema.schema
            val targetDefinition = documentDefinitionService.findBy(targetId).orElseThrow()
            val targetSchema = targetDefinition.schema.schema

            val modifiedContent = try {
                getModifiedContent(migrationRequest).map { (_, modifiedContent) -> modifiedContent }
            } catch (e: DocumentMigrationPatchException) {
                return@withLoggingContext DocumentMigrationConflictResponse.of(
                    migrationRequest = migrationRequest,
                    conflicts = listOf(e.getConflict()),
                )
            } catch (e: Exception) {
                return@withLoggingContext DocumentMigrationConflictResponse.of(
                    migrationRequest = migrationRequest,
                    errors = listOf(e.localizedMessage),
                )
            }
            val jsonPointers = modifiedContent
                .flatMap { it.asJson().getJsonPointers() }
                .distinct()
                .filter { jsonPointer -> !migrationRequest.patches.any { it.target == jsonPointer.toString() } }

            val conflicts = jsonPointers.mapNotNull { jsonPointer ->
                val propertySourceSchema = sourceSchema.getProperty(jsonPointer.toString())
                val propertyTargetSchema = targetSchema.getProperty(jsonPointer.toString())
                if (propertySourceSchema != null && propertySourceSchema != propertyTargetSchema) {
                    DocumentMigrationConflict(
                        jsonPointer.toString(),
                        if (propertyTargetSchema == null) null else jsonPointer.toString(),
                        if (propertyTargetSchema == null) "No longer exists" else "Type changed"
                    )
                } else {
                    null
                }
            }

            val errors = modifiedContent.flatMap { content ->
                targetDefinition.validate(content).validationErrors().map { error -> error.toString() }
            }.distinct()

            return@withLoggingContext DocumentMigrationConflictResponse.of(
                migrationRequest = migrationRequest,
                conflicts = conflicts,
                errors = errors,
                documentCount = modifiedContent.size,
            )
        }
    }

    fun migrateDocuments(migrationRequest: DocumentMigrationRequest) {
        return withLoggingContext(JsonSchemaDocumentDefinition::class.java, migrationRequest.getDocumentDefinitionIdSource().toString()) {
            val targetId = migrationRequest.getDocumentDefinitionIdTarget()
            val targetDefinition = documentDefinitionService.findBy(targetId).orElseThrow()

            getModifiedContent(migrationRequest).forEach { (sourceDocument, modifiedContent) ->
                val result = sourceDocument.applyModifiedContent(modifiedContent, targetDefinition)
                check(result.errors().isEmpty()) { result.errors().joinToString { it.toString() } }
                val targetDocument = result.resultingDocument().orElseThrow()
                targetDocument.setDefinitionId(targetId)
                documentRepository.save(targetDocument)
            }
        }
    }

    fun getModifiedContent(migrationRequest: DocumentMigrationRequest): List<Pair<JsonSchemaDocument, JsonDocumentContent>> {
        val sourceId = migrationRequest.getDocumentDefinitionIdSource()
        val targetId = migrationRequest.getDocumentDefinitionIdTarget()
        val targetDefinition = documentDefinitionService.findBy(targetId).orElseThrow()
        val targetSchema = targetDefinition.schema.schema
        val sourceDocuments = documentRepository.findAll(byDocumentDefinitionId(sourceId))

        return sourceDocuments.map { sourceDocument ->
            val targetJsonBuilder = JsonPatchBuilder()
            val targetJson = sourceDocument.content().asJson()
            migrationRequest.patches.forEach { patch ->
                JsonPatchService.apply(
                    targetJsonBuilder.build(),
                    targetJson,
                    JsonPatchFilterFlag.allowRemovalOperations()
                )
                applyPatch(
                    sourceJson = sourceDocument.content().asJson(),
                    targetJson = targetJson,
                    targetJsonBuilder = targetJsonBuilder,
                    targetSchema = targetSchema,
                    patch = patch,
                )
            }

            val modifiedContent = JsonDocumentContent.build(targetJson, targetJson, targetJsonBuilder.build())
            Pair(sourceDocument, modifiedContent)
        }
    }

    fun applyPatch(
        sourceJson: JsonNode,
        targetJson: JsonNode,
        targetJsonBuilder: JsonPatchBuilder,
        targetSchema: Schema,
        patch: DocumentMigrationPatch
    ) {
        try {
            val sourceValue = getSourceValue(sourceJson, targetJson, targetJsonBuilder, patch)
            if (sourceValue.isMissing()) {
                return
            }
            if (patch.sourceIsRemoved() || patch.sourceIsMoved()) {
                targetJsonBuilder.remove(JsonPointer.valueOf(patch.source))
            }

            if (patch.targetIsSpelExpression()) {
                val contextMap = getSpelContextMap(
                    targetJsonBuilder,
                    sourceJson,
                    targetJson,
                    patch,
                    sourceValue
                )
                handleSpelExpression(patch.target!!, contextMap)
            } else if (patch.targetIsJsonPointer()) {
                val targetPointer = JsonPointer.valueOf(patch.target)
                val targetValue = getTargetValue(targetSchema, patch.target!!, sourceValue)
                targetJsonBuilder.addJsonNodeValue(targetJson, targetPointer, targetValue)
            }
        } catch (exception: Exception) {
            throw DocumentMigrationPatchException(patch, exception)
        }
    }

    fun handleSpelExpression(expression: String, contextMap: Map<String, Any?>): Any? {
        val parserContext = TemplateParserContext("\${", "}")

        val jsonPropertyAccessor = JsonPropertyAccessor()
        jsonPropertyAccessor.setObjectMapper(objectMapper)
        val evaluationContext = StandardEvaluationContext()
        evaluationContext.addPropertyAccessor(MapAccessor())
        evaluationContext.addPropertyAccessor(jsonPropertyAccessor)

        return SpelExpressionParser()
            .parseExpression(expression, parserContext)
            .getValue(evaluationContext, contextMap, Any::class.java)
    }

    fun getSpelContextMap(
        builder: JsonPatchBuilder,
        source: JsonNode,
        target: JsonNode,
        patch: DocumentMigrationPatch,
        sourceValue: SourceValue? = null
    ): MutableMap<String, Any?> {
        val contextMap: MutableMap<String, Any?> = applicationContext.getBeansWithAnnotation(PublicBean::class.java)
        contextMap["source"] = source
        contextMap["target"] = target
        contextMap["builder"] = builder
        contextMap["patch"] = patch
        contextMap["sourceValue"] = sourceValue?.value
        return contextMap
    }

    fun getSourceValue(
        sourceJson: JsonNode,
        targetJson: JsonNode,
        targetJsonBuilder: JsonPatchBuilder,
        patch: DocumentMigrationPatch
    ): SourceValue {
        return if (patch.sourceIsJsonPointer()) {
            SourceValue(value = sourceJson.at(patch.source))
        } else if (patch.sourceIsSpelExpression()) {
            val contextMap = getSpelContextMap(targetJsonBuilder, sourceJson, targetJson, patch)
            SourceValue(value = handleSpelExpression(patch.source, contextMap))
        } else {
            check(patch.sourceIsDefaultValue())
            SourceValue(value = patch.source, specialTypeConversion = true)
        }
    }

    fun getTargetValue(targetSchema: Schema, targetPath: String, sourceValue: SourceValue): JsonNode {
        val targetPropertySchema = targetSchema.getProperty(targetPath)
        return if (targetPropertySchema != null) {
            objectMapper.valueToTree(sourceValue.castToTargetType(objectMapper, targetPropertySchema))
        } else {
            objectMapper.valueToTree(sourceValue.value)
        }
    }

    data class SourceValue(
        val value: Any? = null,
        val specialTypeConversion: Boolean = false // when true, will convert String '[5]' to an array. Not a String.
    ) {
        fun isMissing(): Boolean = value is MissingNode

        fun castToTargetType(objectMapper: ObjectMapper, targetPropertySchema: Schema): Any? {
            val targetType = targetPropertySchema.getTypeReference()
            return if (value is String && specialTypeConversion) {
                try {
                    objectMapper.readValue(value, targetType)
                } catch (_: Exception) {
                    objectMapper.convertValue(value, targetType)
                }
            } else {
                objectMapper.convertValue(value, targetType)
            }
        }
    }
}

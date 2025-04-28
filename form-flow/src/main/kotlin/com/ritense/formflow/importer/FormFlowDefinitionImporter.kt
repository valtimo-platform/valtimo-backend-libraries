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

package com.ritense.formflow.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.configuration.FormFlowDefinition
import com.ritense.formflow.expression.ExpressionProcessorFactoryHolder
import com.ritense.formflow.service.FormFlowService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.FORM
import com.ritense.importer.ValtimoImportTypes.Companion.FORM_FLOW
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import mu.KotlinLogging
import mu.withLoggingContext
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional

@Transactional
class FormFlowDefinitionImporter(
    private val resourceLoader: ResourceLoader,
    private val formFlowService: FormFlowService,
    private val objectMapper: ObjectMapper
) : Importer {
    override fun type() = FORM_FLOW

    override fun dependsOn() = setOf(FORM)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val formFlowKey = FILENAME_REGEX.matchEntire(request.fileName)!!.groupValues[1]
        deploy(formFlowKey, request.content.toString(Charsets.UTF_8), request.caseDefinitionId!!)
    }

    fun isAutoDeployed(formFlowDefinitionKey: String): Boolean {
        withLoggingContext("formFlowDefinitionKey" to formFlowDefinitionKey) {
            return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources(FORM_FLOW_DEFINITIONS_PATH.replace("{formFlowKey}", formFlowDefinitionKey))
                .size > 0
        }
    }

    fun deploy(formFlowKey: String, formFlowJson: String, caseDefinitionId: CaseDefinitionId) {
        withLoggingContext("formFlowDefinitionKey" to formFlowKey) {
            validate(formFlowJson)

            val formFlowDefinitionConfig = objectMapper.readValue(formFlowJson, FormFlowDefinition::class.java)

            validate(formFlowDefinitionConfig)

            try {
                val existingDefinition = formFlowService.findDefinitionOrNull(formFlowKey, caseDefinitionId)
                var definitionId = FormFlowDefinitionId.newId(formFlowKey, caseDefinitionId)

                if (existingDefinition != null) {
                    if (formFlowDefinitionConfig.contentEquals(existingDefinition)) {
                        logger.info("Form Flow already deployed - {}", definitionId.toString())
                        return
                    }
                }

                formFlowService.save(formFlowDefinitionConfig.toDefinition(definitionId))
                logger.info("Deployed Form Flow - {}", definitionId.toString())
            } catch (e: Exception) {
                throw RuntimeException("Failed to deploy Form Flow $formFlowKey", e)
            }
        }
    }

    private fun validate(formFlowJson: String) {
        val definitionJsonObject = JSONObject(JSONTokener(formFlowJson))

        val schema = SchemaLoader.load(JSONObject(JSONTokener(loadFormFlowSchemaResource().inputStream)))
        schema.validate(definitionJsonObject)
    }

    private fun validate(formFlowDefinitionConfig: FormFlowDefinition) {
        val expressionProcessor = ExpressionProcessorFactoryHolder.getInstance().create()
        formFlowDefinitionConfig.steps.forEach { step ->
            step.onBack.forEach { expression -> expressionProcessor.validate(expression) }
            step.onOpen.forEach { expression -> expressionProcessor.validate(expression) }
            step.onComplete.forEach { expression -> expressionProcessor.validate(expression) }
        }
    }

    private fun loadFormFlowSchemaResource(): Resource {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(FORM_FLOW_SCHEMA_PATH)
    }

    private companion object {
        private const val FORM_FLOW_SCHEMA_PATH = "classpath:config/form-flow/schema/formflow.schema.json"
        private const val FORM_FLOW_DEFINITIONS_PATH = "classpath:config/case/*/*/form-flow/{formFlowKey}.json"
        val FILENAME_REGEX = """/form-flow/([^/]+)\.json""".toRegex()
        val logger = KotlinLogging.logger {}
    }
}
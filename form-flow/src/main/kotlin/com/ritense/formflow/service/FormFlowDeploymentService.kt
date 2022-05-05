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

package com.ritense.formflow.service

import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.configuration.FormFlowDefinition
import com.ritense.formflow.expression.ExpressionProcessorFactory
import mu.KotlinLogging
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.util.StreamUtils
import java.io.InputStream
import java.nio.charset.StandardCharsets

class FormFlowDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val formFlowService: FormFlowService,
    private val expressionProcessorFactory: ExpressionProcessorFactory,
    private val formFlowObjectmapper: FormFlowObjectMapper
) {

    private val FORM_FLOW_SCHEMA_PATH = "classpath:config/form-flow/schema/formflow.schema.json"
    private val FORM_FLOW_DEFINITIONS_PATH = "classpath:config/form-flow/*.json"

    @EventListener(ApplicationReadyEvent::class)
    fun deployAll() {
        logger.info("Deploy all Form Flow definitions")
        try {
            loadFormFlowDefinitionsResources().forEach { resource ->
                if (resource.filename != null) {
                    deploy(resource)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Error deploying Form Flow's", e)
        }
    }

    private fun deploy(formFlow: Resource) {
        deploy(formFlow.filename!!.substringBeforeLast("."), formFlow.inputStream)
    }

    private fun deploy(formFlowKey: String, formFlowJson: InputStream) {
        deploy(formFlowKey, StreamUtils.copyToString(formFlowJson, StandardCharsets.UTF_8))
    }

    fun deploy(formFlowKey: String, formFlowJson: String) {
        validate(formFlowJson)

        val formFlowDefinitionConfig = formFlowObjectmapper.get().readValue(formFlowJson, FormFlowDefinition::class.java)

        validate(formFlowDefinitionConfig)

        try {
            val existingDefinition = formFlowService.findLatestDefinitionByKey(formFlowKey)
            var definitionId = FormFlowDefinitionId.newId(formFlowKey)

            if (existingDefinition != null) {
                if (formFlowDefinitionConfig.contentEquals(existingDefinition)) {
                    logger.info("Form Flow already deployed - {}", definitionId.toString())
                    return
                } else {
                    definitionId = FormFlowDefinitionId.nextVersion(existingDefinition.id)
                    logger.info("Form Flow changed. Deploying next version - {}", definitionId.toString())
                }
            }

            formFlowService.save(formFlowDefinitionConfig.toDefinition(definitionId))
            logger.info("Deployed Form Flow - {}", definitionId.toString())
        } catch (e: Exception) {
            throw RuntimeException("Failed to deploy Form Flow $formFlowKey", e)
        }
    }

    private fun validate(formFlowJson: String) {
        val definitionJsonObject = JSONObject(JSONTokener(formFlowJson))

        val schema = SchemaLoader.load(JSONObject(JSONTokener(loadFormFlowSchemaResource().inputStream)))
        schema.validate(definitionJsonObject)
    }

    private fun validate(formFlowDefinitionConfig: FormFlowDefinition) {
        val expressionProcessor = expressionProcessorFactory.create()
        formFlowDefinitionConfig.steps.forEach { step ->
            step.onOpen?.forEach { expression ->
                expressionProcessor.validate(expression)
            }
        }
    }

    private fun loadFormFlowSchemaResource(): Resource {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(FORM_FLOW_SCHEMA_PATH)
    }

    private fun loadFormFlowDefinitionsResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(FORM_FLOW_DEFINITIONS_PATH)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
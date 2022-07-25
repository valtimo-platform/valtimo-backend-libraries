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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.exception.PluginPropertyParseException
import com.ritense.plugin.exception.PluginPropertyRequiredException
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.web.rest.dto.PluginActionDefinitionDto
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkCreateDto
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkResultDto
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkUpdateDto
import com.ritense.valtimo.contract.json.Mapper
import java.lang.reflect.Method
import javax.validation.ValidationException
import mu.KotlinLogging

class PluginService(
    private val pluginDefinitionRepository: PluginDefinitionRepository,
    private val pluginConfigurationRepository: PluginConfigurationRepository,
    private val pluginActionDefinitionRepository: PluginActionDefinitionRepository,
    private val pluginProcessLinkRepository: PluginProcessLinkRepository,
    private val pluginFactories: List<PluginFactory<*>>,
    private val objectMapper: ObjectMapper
) {

    fun getPluginDefinitions(): List<PluginDefinition> {
        return pluginDefinitionRepository.findAll()
    }

    fun getPluginConfigurations(): List<PluginConfiguration> {
        return pluginConfigurationRepository.findAll()
    }

    fun createPluginConfiguration(
        title: String,
        properties: JsonNode,
        pluginDefinitionKey: String
    ): PluginConfiguration {
        val pluginDefinition = pluginDefinitionRepository.getById(pluginDefinitionKey)
        validateProperties(properties, pluginDefinition)

        return pluginConfigurationRepository.save(
            PluginConfiguration(PluginConfigurationId.newId(), title, properties, pluginDefinition)
        )
    }

    fun updatePluginConfiguration(
        pluginConfigurationId: PluginConfigurationId,
        title: String,
        properties: JsonNode,
    ): PluginConfiguration {
        val pluginDefinition = pluginConfigurationRepository.getById(pluginConfigurationId)

        pluginDefinition.title = title
        pluginDefinition.updateProperties(properties)

        return pluginConfigurationRepository.save(pluginDefinition)
    }

    fun deletePluginConfiguration(
        pluginConfigurationId: PluginConfigurationId
    ) {
        pluginConfigurationRepository.deleteById(pluginConfigurationId)
    }

    fun getPluginDefinitionActions(
        pluginDefinitionKey: String,
        activityType: ActivityType?
    ): List<PluginActionDefinitionDto> {
        val actions = if (activityType == null)
            pluginActionDefinitionRepository.findByIdPluginDefinitionKey(pluginDefinitionKey)
        else
            pluginActionDefinitionRepository.findByIdPluginDefinitionKeyAndActivityTypes(pluginDefinitionKey, activityType)

        return actions.map {
            PluginActionDefinitionDto(
                it.id.key,
                it.title,
                it.description
            )
        }
    }

    fun getProcessLinks(
        processDefinitionId: String,
        activityId: String
    ): List<PluginProcessLinkResultDto> {
        return pluginProcessLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinitionId, activityId)
            .map {
                PluginProcessLinkResultDto(
                    id = it.id.id,
                    processDefinitionId = it.processDefinitionId,
                    activityId = it.activityId,
                    pluginConfigurationId = it.pluginConfigurationId.id,
                    pluginActionDefinitionKey = it.pluginActionDefinitionKey,
                    actionProperties = it.actionProperties
                )
            }
    }

    fun createProcessLink(processLink: PluginProcessLinkCreateDto) {
        if (getProcessLinks(processLink.processDefinitionId, processLink.activityId).isNotEmpty()) {
            throw ValidationException("A process-link for this process-definition and activity already exists!")
        }

        val newProcessLink = PluginProcessLink(
            id = PluginProcessLinkId.newId(),
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            actionProperties = processLink.actionProperties,
            pluginConfigurationId = PluginConfigurationId.existingId(processLink.pluginConfigurationId),
            pluginActionDefinitionKey = processLink.pluginActionDefinitionKey
        )
        pluginProcessLinkRepository.save(newProcessLink)
    }

    fun updateProcessLink(processLink: PluginProcessLinkUpdateDto) {
        val link = pluginProcessLinkRepository.getById(
            PluginProcessLinkId.existingId(processLink.id)
        ).copy(
                actionProperties = processLink.actionProperties,
                pluginConfigurationId = PluginConfigurationId.existingId(processLink.pluginConfigurationId),
                pluginActionDefinitionKey = processLink.pluginActionDefinitionKey
            )
        pluginProcessLinkRepository.save(link)
    }

    fun invoke(executionContext:Any, processLink: PluginProcessLink) {
        val configuration = pluginConfigurationRepository.getById(processLink.pluginConfigurationId)
        val instance = createPluginInstance(configuration)

        val method = getActionMethod(instance, processLink)
        val methodArguments = resolveMethodArguments(method, executionContext, processLink.actionProperties)
        method.invoke(instance, *methodArguments)
    }

    private fun resolveMethodArguments(method: Method, executionContext: Any, actionProperties: JsonNode?): Array<Any?> {

        return method.parameters.map { param ->
            if (param.isAnnotationPresent(PluginActionProperty::class.java)) {
                if (actionProperties == null) {
                    null
                } else {
                    getTypedProperty(param.name, param.type, actionProperties)
                }
            } else if (param.type.isInstance(executionContext)) {
                executionContext
            } else {
                // TODO: Implement some argument resolver using the given executionContext?
                throw RuntimeException("Could not resolve parameter ${param.name}")
            }
        }.toTypedArray()
    }

    private fun getTypedProperty(name: String, type: Class<*>?, actionProperties: JsonNode): Any? {
        val value = actionProperties.get(name)
        if (value.isNull) {
            return null
        }
        return objectMapper.treeToValue(value, type)
    }

    private fun createPluginInstance(configuration: PluginConfiguration): Any {
        return  pluginFactories.first {
            it.canCreate(configuration)
        }.create(configuration)!!
    }

    private fun getActionMethod(
        instance: Any,
        processLink: PluginProcessLink
    ): Method {
        val method = instance.javaClass.methods.filter { method ->
            method.isAnnotationPresent(PluginAction::class.java)
        }.associateWith { method -> method.getAnnotation(PluginAction::class.java) }
            .filter { (_, annotation) -> annotation.key == processLink.pluginActionDefinitionKey }
            .map { entry -> entry.key }
            .first()
        return method
    }

    private fun validateProperties(properties: JsonNode, pluginDefinition: PluginDefinition) {
        assert(properties.isObject)

        val errors = mutableListOf<Throwable>()
        pluginDefinition.pluginProperties.forEach { pluginProperty ->
            val propertyNode = properties[pluginProperty.fieldName]

            if (propertyNode == null || propertyNode.isMissingNode || propertyNode.isNull) {
                if (pluginProperty.required) {
                    errors.add(PluginPropertyRequiredException(pluginProperty.fieldName, pluginDefinition.title))
                }
            } else {
                try {
                    val propertyClass = Class.forName(pluginProperty.fieldType)
                    val property = Mapper.INSTANCE.get().treeToValue(propertyNode, propertyClass)
                    assert(property != null)
                } catch (e: Exception) {
                    errors.add(PluginPropertyParseException(pluginProperty.fieldName, pluginDefinition.title, e))
                }
            }
        }

        if (errors.isNotEmpty()) {
            errors.forEach { logger.error { it } }
            throw errors.first()
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

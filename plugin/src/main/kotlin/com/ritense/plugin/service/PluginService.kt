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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginCategory
import com.ritense.plugin.annotation.PluginEvent
import com.ritense.plugin.autodeployment.PluginAutoDeploymentDto
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.EventType
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.exception.PluginEventInvocationException
import com.ritense.plugin.exception.PluginPropertyParseException
import com.ritense.plugin.exception.PluginPropertyRequiredException
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginConfigurationSearchRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.web.rest.request.PluginProcessLinkCreateDto
import com.ritense.plugin.web.rest.request.PluginProcessLinkUpdateDto
import com.ritense.plugin.web.rest.result.PluginActionDefinitionDto
import com.ritense.plugin.web.rest.result.PluginProcessLinkResultDto
import com.ritense.valueresolver.ValueResolverService
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.data.repository.findByIdOrNull
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.UUID
import javax.validation.ConstraintViolationException
import javax.validation.ValidationException
import javax.validation.Validator
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

class PluginService(
    private val pluginDefinitionRepository: PluginDefinitionRepository,
    private val pluginConfigurationRepository: PluginConfigurationRepository,
    private val pluginActionDefinitionRepository: PluginActionDefinitionRepository,
    private val pluginProcessLinkRepository: PluginProcessLinkRepository,
    private val pluginFactories: List<PluginFactory<*>>,
    private val objectMapper: ObjectMapper,
    private val valueResolverService: ValueResolverService,
    private val pluginConfigurationSearchRepository: PluginConfigurationSearchRepository,
    private val validator: Validator,
) {

    fun getObjectMapper(): ObjectMapper {
        return objectMapper
    }

    fun getPluginDefinitions(): List<PluginDefinition> {
        return pluginDefinitionRepository.findAllByOrderByTitleAsc()
    }

    fun getPluginConfigurations(
        pluginConfigurationSearchParameters: PluginConfigurationSearchParameters
    ): List<PluginConfiguration> {
        return pluginConfigurationSearchRepository.search(pluginConfigurationSearchParameters)
    }

    fun createPluginConfiguration(
        title: String,
        properties: ObjectNode,
        pluginDefinitionKey: String
    ): PluginConfiguration {
        val pluginDefinition = pluginDefinitionRepository.getById(pluginDefinitionKey)
        validateProperties(properties, pluginDefinition)

        val pluginConfiguration = pluginConfigurationRepository.save(
            PluginConfiguration(PluginConfigurationId.newId(), title, properties, pluginDefinition)
        )

        try {
            pluginConfiguration.runAllPluginEvents(EventType.CREATE)

        } catch (e: Exception) {
            pluginConfigurationRepository.deleteById(pluginConfiguration.id)
            throw PluginEventInvocationException(pluginConfiguration, e)
        }

        return pluginConfiguration
    }

    fun deployPluginConfigurations(deploymentDto: PluginAutoDeploymentDto) {
        val plugin: PluginConfiguration
        val pluginDefinition = pluginDefinitionRepository.getById(deploymentDto.pluginDefinitionKey)
        if (deploymentDto.id != null && pluginConfigurationRepository.existsById(
                PluginConfigurationId.existingId(deploymentDto.id)
            )
        ) {
            deletePluginConfiguration(PluginConfigurationId.existingId(deploymentDto.id))
        }
        val resolvedProperties = resolveProperties(deploymentDto.properties)
        validateProperties(resolvedProperties, pluginDefinition)
        val pluginId = pluginConfigurationRepository.saveAndFlush(
            PluginConfiguration(
                deploymentDto.id?.let { PluginConfigurationId.existingId(it) } ?: PluginConfigurationId.newId(),
                deploymentDto.title,
                resolvedProperties,
                pluginDefinition
            )
        ).id
        plugin = pluginConfigurationRepository.findById(pluginId).orElseThrow()
        try {
            plugin.runAllPluginEvents(EventType.CREATE)
        } catch (e: Exception) {
            pluginConfigurationRepository.deleteById(plugin.id)
            throw PluginEventInvocationException(plugin, e)
        }
    }

    private fun resolveProperties(properties: ObjectNode?): ObjectNode {
        val result = objectMapper.createObjectNode()
        properties?.fields()?.forEachRemaining {
            result.replace(it.key, resolveValue(it.value))
        }
        return result
    }

    private fun resolveValue(node: JsonNode?): JsonNode? {
        if (node != null) {
            if (node is ObjectNode) {
                return resolveProperties(node)
            } else if (node.isArray) {
                return objectMapper.createArrayNode().addAll(node.map { resolveValue(it) })
            } else if (node.isTextual) {
                var value = node.textValue()
                Regex("\\$\\{([^\\}]+)\\}").findAll(value)
                    .map { it.groupValues }
                    .forEach { (placeholder, placeholderValue) ->
                        val resolvedValue = System.getenv(placeholderValue)
                            ?: System.getProperty(placeholderValue)
                            ?: throw IllegalStateException("Failed to find environment variable: '$placeholderValue'")
                        value = value.replace(placeholder, resolvedValue)
                    }
                return TextNode(value)
            }
        }
        return node
    }

    fun updatePluginConfiguration(
        pluginConfigurationId: PluginConfigurationId,
        title: String,
        properties: ObjectNode,
    ): PluginConfiguration {
        val pluginConfiguration = pluginConfigurationRepository.getById(pluginConfigurationId)

        pluginConfiguration.title = title
        pluginConfiguration.updateProperties(properties)

        validateProperties(pluginConfiguration.properties!!, pluginConfiguration.pluginDefinition)

        try {
            pluginConfiguration.runAllPluginEvents(EventType.UPDATE)
        } catch (e: Exception) {
            throw PluginEventInvocationException(pluginConfiguration, e)
        }

        return pluginConfigurationRepository.save(pluginConfiguration)
    }

    fun deletePluginConfiguration(
        pluginConfigurationId: PluginConfigurationId
    ) {
        pluginConfigurationRepository.findByIdOrNull(pluginConfigurationId)
            ?.let {
                try {
                    it.runAllPluginEvents(EventType.DELETE)
                } catch (e: Exception) {
                    logger.warn { "Failed to run events on plugin ${it.title} with id ${it.id.id}" }
                }
            }
            ?: logger.warn { "Plugin configuration with Id: [$pluginConfigurationId] was not found." }

        pluginConfigurationRepository.deleteById(pluginConfigurationId)
    }

    fun getPluginDefinitionActions(
        pluginDefinitionKey: String,
        activityType: ActivityType?
    ): List<PluginActionDefinitionDto> {
        val actions = if (activityType == null)
            pluginActionDefinitionRepository.findByIdPluginDefinitionKey(pluginDefinitionKey)
        else
            pluginActionDefinitionRepository.findByIdPluginDefinitionKeyAndActivityTypes(
                pluginDefinitionKey,
                activityType
            )

        return actions.map {
            PluginActionDefinitionDto(
                it.id.key,
                it.title,
                it.description
            )
        }
    }

    @Deprecated("Marked for removal since 10.6.0", ReplaceWith("processLinkService.processLinkExists(i,j,k)"))
    fun processLinkExists(
        pluginConfigurationId: PluginConfigurationId,
        activityId: String,
        activityType: ActivityType
    ): Boolean {
        return pluginProcessLinkRepository
            .findByPluginConfigurationIdAndActivityIdAndActivityType(
                pluginConfigurationId,
                activityId,
                activityType
            ).size == 1
    }

    @Deprecated("Marked for removal since 10.6.0", ReplaceWith("processLinkService.getProcessLinks(i,j)"))
    fun getProcessLinks(
        processDefinitionId: String,
        activityId: String
    ): List<PluginProcessLinkResultDto> {
        return pluginProcessLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinitionId, activityId)
            .map {
                PluginProcessLinkResultDto(
                    id = it.id,
                    processDefinitionId = it.processDefinitionId,
                    activityId = it.activityId,
                    activityType = it.activityType,
                    pluginConfigurationId = it.pluginConfigurationId.id,
                    pluginActionDefinitionKey = it.pluginActionDefinitionKey,
                    actionProperties = it.actionProperties
                )
            }
    }

    @Deprecated("Marked for removal since 10.6.0", ReplaceWith("processLinkService.createProcessLink(i)"))
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
            pluginActionDefinitionKey = processLink.pluginActionDefinitionKey,
            activityType = ActivityType.fromValue(processLink.activityType.value).mapOldActivityTypeToCurrent()
        )
        pluginProcessLinkRepository.save(newProcessLink)
    }

    @Deprecated("Marked for removal since 10.6.0", ReplaceWith("processLinkService.updateProcessLink(i)"))
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

    @Deprecated("Marked for removal since 10.6.0", ReplaceWith("processLinkService.deleteProcessLink(id)"))
    fun deleteProcessLink(id: UUID) {
        pluginProcessLinkRepository.deleteById(PluginProcessLinkId.existingId(id))
    }

    fun invoke(execution: DelegateExecution, processLink: PluginProcessLink): Any? {
        val instance: Any = createInstance(processLink.pluginConfigurationId)

        val method = getActionMethod(instance, processLink)
        val methodArguments = resolveMethodArguments(method, execution, processLink.actionProperties)

        logger.debug { "Invoking method ${method.name} of class ${instance.javaClass.simpleName} for activity ${execution.currentActivityId} of process-instance ${execution.processInstanceId}" }

        return method.invoke(instance, *methodArguments)
    }

    fun invoke(task: DelegateTask, processLink: PluginProcessLink): Any? {
        val instance: Any = createInstance(processLink.pluginConfigurationId)

        val method = getActionMethod(instance, processLink)
        val methodArguments = resolveMethodArguments(method, task, processLink.actionProperties)

        logger.debug { "Invoking method ${method.name} of class ${instance.javaClass.simpleName} for task ${task.taskDefinitionKey} of process-instance ${task.processInstanceId}" }

        return method.invoke(instance, *methodArguments)
    }

    private fun resolveMethodArguments(
        method: Method,
        execution: DelegateExecution,
        actionProperties: ObjectNode?
    ): Array<Any?> {

        val actionParamValueMap = resolveActionParamValues(execution, method, actionProperties)

        return method.parameters.map { param ->
            actionParamValueMap[param]
                ?: if (param.type.isInstance(execution)) {
                    execution
                } else {
                    null
                }

        }.toTypedArray()
    }

    private fun resolveMethodArguments(
        method: Method,
        task: DelegateTask,
        actionProperties: ObjectNode?
    ): Array<Any?> {

        val actionParamValueMap = resolveActionParamValues(task, method, actionProperties)

        return method.parameters.map { param ->
            actionParamValueMap[param]
                ?: if (param.type.isInstance(task)) {
                    task
                } else {
                    null
                }

        }.toTypedArray()
    }

    private fun resolveActionParamValues(
        execution: DelegateExecution,
        method: Method,
        actionProperties: ObjectNode?
    ): Map<Parameter, Any> {
        if (actionProperties == null) {
            return mapOf()
        }

        val paramValues = method.parameters.filter { param ->
            param.isAnnotationPresent(PluginActionProperty::class.java)
        }.mapNotNull { param ->
            param to actionProperties.get(param.name)
        }.filter { pair ->
            pair.second != null
        }.toMap()

        // We want to process all placeholder values together to improve performance if external sources are needed.
        val placeHolderValueMap = method.parameters.filter { param ->
            param.isAnnotationPresent(PluginActionProperty::class.java)
        }.mapNotNull { param ->
            param to actionProperties.get(param.name)
        }.toMap()
            .filterValues { it != null && it.isTextual }
            .mapValues {
                it.value.textValue()
            }.run {
                // Resolve all string values, which might or might not be placeholders.
                valueResolverService.resolveValues(execution.processInstanceId, execution, values.toList())
            }

        return mapActionParamValues(paramValues, placeHolderValueMap)
    }

    private fun resolveActionParamValues(
        task: DelegateTask,
        method: Method,
        actionProperties: ObjectNode?
    ): Map<Parameter, Any> {
        if (actionProperties == null) {
            return mapOf()
        }

        val paramValues = method.parameters.filter { param ->
            param.isAnnotationPresent(PluginActionProperty::class.java)
        }.mapNotNull { param ->
            param to actionProperties.get(param.name)
        }.filter { pair ->
            pair.second != null
        }.toMap()

        // We want to process all placeholder values together to improve performance if external sources are needed.
        val placeHolderValueMap = method.parameters.filter { param ->
            param.isAnnotationPresent(PluginActionProperty::class.java)
        }.mapNotNull { param ->
            param to actionProperties.get(param.name)
        }.toMap()
            .filterValues { it != null && it.isTextual }
            .mapValues {
                it.value.textValue()
            }.run {
                // Resolve all string values, which might or might not be placeholders.
                valueResolverService.resolveValues(task.execution.processInstanceId, task.execution, values.toList())
            }

        return mapActionParamValues(paramValues, placeHolderValueMap)
    }

    private fun mapActionParamValues(
        paramValues: Map<Parameter, JsonNode>,
        placeHolderValueMap: Map<String, Any>
    ): Map<Parameter, Any> {
        return paramValues.mapValues { (param, value) ->
            if (value.isTextual) {
                val placeHolderValueAsString = placeHolderValueMap[value.textValue()]
                if (placeHolderValueAsString != null) {
                    objectMapper.convertValue(
                        placeHolderValueAsString,
                        objectMapper.constructType(param.parameterizedType)
                    )
                } else {
                    toValue(value, param)
                }
            } else {
                toValue(value, param)
            }
        }
    }

    private fun <T> toValue(value: JsonNode, param: Parameter): T {
        return objectMapper.treeToValue(value, objectMapper.constructType(param.parameterizedType))
    }

    fun <T> createInstance(pluginConfigurationId: String): T {
        return createInstance(UUID.fromString(pluginConfigurationId))
    }

    fun <T> createInstance(pluginConfigurationId: UUID): T {
        return createInstance(PluginConfigurationId.existingId(pluginConfigurationId)) as T
    }

    fun createInstance(pluginConfigurationId: PluginConfigurationId): Any {
        val configuration = pluginConfigurationRepository.getById(pluginConfigurationId)
        return createInstance(configuration)
    }

    fun createInstance(pluginConfiguration: PluginConfiguration): Any {
        return pluginFactories.first {
            it.canCreate(pluginConfiguration)
        }.create(pluginConfiguration)
    }

    fun <T> createInstance(clazz: Class<T>, configurationFilter: (JsonNode) -> Boolean): T? {
        val pluginConfiguration = findPluginConfiguration(clazz, configurationFilter)

        return pluginConfiguration?.let { createInstance(it) as T }
    }

    fun <T> findPluginConfiguration(clazz: Class<T>, configurationFilter: (JsonNode) -> Boolean): PluginConfiguration? {
        val annotation = clazz.getAnnotation(Plugin::class.java)
            ?: throw IllegalArgumentException("Requested plugin for class ${clazz.name}, but class is not annotated as plugin")

        return findPluginConfiguration(annotation.key, configurationFilter)
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

    private fun validateProperties(properties: ObjectNode, pluginDefinition: PluginDefinition) {
        val errors = mutableListOf<Throwable>()
        val pluginClass = Class.forName(pluginDefinition.fullyQualifiedClassName)
        pluginDefinition.properties.forEach { pluginProperty ->
            val propertyNode = properties[pluginProperty.fieldName]

            val propertyIsNull = propertyNode == null || propertyNode.isMissingNode || propertyNode.isNull
            val propertyIsEmpty = propertyNode is TextNode && propertyNode.textValue() == ""
            if ((propertyIsNull || propertyIsEmpty) && pluginProperty.required) {
                errors.add(PluginPropertyRequiredException(pluginProperty.fieldName, pluginDefinition.title))
            } else {
                try {
                    val propertyClass = Class.forName(pluginProperty.fieldType)
                    val propertyClassIsPlugin = propertyClass.isAnnotationPresent(Plugin::class.java)
                    val propertyClassIsPluginCategory = propertyClass.isAnnotationPresent(PluginCategory::class.java)
                    if (propertyClassIsPlugin || propertyClassIsPluginCategory) {
                        val propertyConfigurationId =
                            PluginConfigurationId.existingId(UUID.fromString(propertyNode.textValue()))
                        val propertyConfiguration = pluginConfigurationRepository.findById(propertyConfigurationId)
                        assert(propertyConfiguration.isPresent) { "Plugin configuration with id ${propertyConfigurationId.id} does not exist!" }
                    } else {
                        val propertyValue = objectMapper.treeToValue(propertyNode, propertyClass)
                        assert(propertyValue != null)
                        val validationErrors =
                            validator.validateValue(pluginClass, pluginProperty.fieldName, propertyValue)
                        if (validationErrors.isNotEmpty()) {
                            throw ConstraintViolationException(validationErrors)
                        }
                    }
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

    fun findPluginConfiguration(
        pluginDefinitionKey: String,
        filter: (JsonNode) -> Boolean
    ): PluginConfiguration? {
        val configurations = pluginConfigurationRepository.findByPluginDefinitionKey(pluginDefinitionKey)
        return configurations.firstOrNull { config ->
            val configProperties = config.properties
            configProperties != null && filter(configProperties)
        }
    }

    private fun PluginConfiguration.runAllPluginEvents(eventType: EventType): PluginConfiguration {
        val pluginInstance = createInstance(this)
        val pluginKlass = pluginInstance.javaClass.kotlin

        pluginKlass
            .functions
            .filter {
                it.findAnnotation<PluginEvent>()
                    ?.invokedOn
                    ?.contains(eventType)
                    ?: false
            }
            .forEach {
                logger.debug { "Running ${eventType.name} event method [${it.name}] of plugin [${this.title}]" }
                it.call(pluginInstance)
            }

        return this
    }

    fun getPluginDefinitionActionsByActivityType(activityType: String): List<PluginActionDefinition> {
        return pluginActionDefinitionRepository.findByActivityTypes(ActivityType.fromValue(activityType))
    }

    companion object {
        val logger = KotlinLogging.logger {}

        const val PROCESS_LINK_TYPE_PLUGIN = "plugin"
    }
}

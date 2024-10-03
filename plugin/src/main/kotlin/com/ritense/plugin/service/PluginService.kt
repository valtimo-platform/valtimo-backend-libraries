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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginCategory
import com.ritense.plugin.annotation.PluginEvent
import com.ritense.plugin.autodeployment.PluginAutoDeploymentDto
import com.ritense.plugin.domain.EventType
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.events.PluginConfigurationDeletedEvent
import com.ritense.plugin.events.PluginConfigurationIdUpdatedEvent
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
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valueresolver.ValueResolverService
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ValidationException
import jakarta.validation.Validator
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.Environment
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.UUID
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

@Transactional
@Service
@SkipComponentScan
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
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val encryptionService: EncryptionService,
    private val environment: Environment
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
        @LoggableResource(resourceType = PluginDefinition::class) pluginDefinitionKey: String
    ): PluginConfiguration {
        return createPluginConfiguration(
            PluginConfigurationId.newId(),
            title,
            properties,
            pluginDefinitionKey
        )
    }

    fun createPluginConfiguration(
        @LoggableResource(resourceType = PluginConfiguration::class) id: PluginConfigurationId,
        title: String,
        properties: ObjectNode,
        @LoggableResource(resourceType = PluginDefinition::class) pluginDefinitionKey: String
    ): PluginConfiguration {
        val pluginDefinition = pluginDefinitionRepository.getReferenceById(pluginDefinitionKey)
        validateProperties(properties, pluginDefinition)

        if (pluginConfigurationRepository.existsById(id)) {
            throw IllegalStateException("Failed to create plugin. Plugin ID '${id.id}' is already used by another plugin.")
        }

        val pluginConfigurationToBeSaved = PluginConfiguration(
            id,
            title,
            properties,
            pluginDefinition,
            encryptionService,
            objectMapper
        )

        val pluginConfiguration = pluginConfigurationRepository.save(pluginConfigurationToBeSaved)

        try {
            pluginConfiguration.runAllPluginEvents(EventType.CREATE)

        } catch (e: Exception) {
            pluginConfigurationRepository.deleteById(pluginConfiguration.id)
            throw PluginEventInvocationException(pluginConfiguration, e)
        }

        return pluginConfiguration
    }

    fun deployPluginConfigurations(deploymentDto: PluginAutoDeploymentDto) {
        withLoggingContext(
            mapOf(
                ProcessLink::class.java.canonicalName to deploymentDto.id.toString(),
                PluginDefinition::class.java.canonicalName to deploymentDto.pluginDefinitionKey
            )
        ) {
            val pluginConfiguration: PluginConfiguration
            var oldConfiguration: PluginConfiguration? = null
            var action: EventType = EventType.CREATE

            val pluginDefinition = pluginDefinitionRepository.getReferenceById(deploymentDto.pluginDefinitionKey)
            val resolvedProperties = resolveProperties(deploymentDto.properties)
            validateProperties(resolvedProperties, pluginDefinition)

            deploymentDto.id?.let {
                oldConfiguration = pluginConfigurationRepository.findByIdOrNull(PluginConfigurationId.existingId(deploymentDto.id))
                if (oldConfiguration != null) {
                    action = EventType.UPDATE
                }
            }

            pluginConfiguration = PluginConfiguration(
                deploymentDto.id?.let { PluginConfigurationId.existingId(it) } ?: PluginConfigurationId.newId(),
                deploymentDto.title,
                resolvedProperties,
                pluginDefinition,
                encryptionService,
                objectMapper
            )

            pluginConfigurationRepository.saveAndFlush(pluginConfiguration)
            try {
                pluginConfiguration.runAllPluginEvents(action)
            } catch (e: Exception) {
                if (oldConfiguration != null) {
                    //restore old configuration
                    pluginConfigurationRepository.save(oldConfiguration)
                } else {
                    pluginConfigurationRepository.deleteById(pluginConfiguration.id)
                }
                throw PluginEventInvocationException(pluginConfiguration, e)
            }
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
                        val resolvedValue = environment.getProperty(placeholderValue)
                            ?: System.getenv(placeholderValue)
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
        @LoggableResource(resourceType = PluginConfiguration::class) pluginConfigurationId: PluginConfigurationId,
        title: String,
        properties: ObjectNode,
    ): PluginConfiguration {
        return updatePluginConfiguration(pluginConfigurationId, pluginConfigurationId, title, properties)
    }

    fun updatePluginConfiguration(
        @LoggableResource(resourceType = PluginConfiguration::class) oldPluginConfigurationId: PluginConfigurationId,
        newPluginConfigurationId: PluginConfigurationId,
        title: String,
        properties: ObjectNode,
    ): PluginConfiguration {
        val pluginConfiguration = updatePluginConfigurationId(oldPluginConfigurationId, newPluginConfigurationId)

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
        @LoggableResource(resourceType = PluginConfiguration::class) pluginConfigurationId: PluginConfigurationId
    ) {
        pluginConfigurationRepository.findByIdOrNull(pluginConfigurationId)
            ?.let {
                try {
                    it.runAllPluginEvents(EventType.DELETE)
                } catch (e: Exception) {
                    logger.warn { "Failed to run events on plugin ${it.title} with id ${it.id.id}" }
                }

                pluginConfigurationRepository.deleteById(pluginConfigurationId)

                val event = PluginConfigurationDeletedEvent(it)
                applicationEventPublisher.publishEvent(event)
            }
            ?: logger.warn { "Plugin configuration with Id: [$pluginConfigurationId] was not found." }
    }

    fun getPluginDefinitionActions(
        @LoggableResource(resourceType = PluginDefinition::class) pluginDefinitionKey: String,
        activityType: ActivityTypeWithEventName?
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
        @LoggableResource(resourceType = PluginConfiguration::class) pluginConfigurationId: PluginConfigurationId,
        activityId: String,
        activityType: ActivityTypeWithEventName
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
        @LoggableResource("com.ritense.valtimo.camunda.domain.CamundaProcessDefinition") processDefinitionId: String,
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
            activityType = processLink.activityType
        )
        pluginProcessLinkRepository.save(newProcessLink)
    }

    @Deprecated("Marked for removal since 10.6.0", ReplaceWith("processLinkService.updateProcessLink(i)"))
    fun updateProcessLink(processLink: PluginProcessLinkUpdateDto) {
        withLoggingContext(ProcessLink::class, processLink.id) {
            val link = pluginProcessLinkRepository.getById(
                PluginProcessLinkId.existingId(processLink.id)
            ).copy(
                actionProperties = processLink.actionProperties,
                pluginConfigurationId = PluginConfigurationId.existingId(processLink.pluginConfigurationId),
                pluginActionDefinitionKey = processLink.pluginActionDefinitionKey
            )
            pluginProcessLinkRepository.save(link)
        }
    }

    @Deprecated("Marked for removal since 10.6.0", ReplaceWith("processLinkService.deleteProcessLink(id)"))
    fun deleteProcessLink(
        @LoggableResource(resourceType = ProcessLink::class) id: UUID
    ) {
        pluginProcessLinkRepository.deleteById(PluginProcessLinkId.existingId(id))
    }

    fun invoke(execution: DelegateExecution, processLink: PluginProcessLink): Any? {
        return withLoggingContext(
            mapOf(
                "com.ritense.document.domain.impl.JsonSchemaDocument" to execution.processBusinessKey,
                "activityId" to execution.currentActivityId,
                "activityName" to execution.currentActivityName,
                ProcessLink::class.java.canonicalName to processLink.id.toString(),
                PluginConfiguration::class.java.canonicalName to processLink.pluginConfigurationId.toString()
            )
        ) {
            val instance: Any = createInstance(processLink.pluginConfigurationId)

            val method = getActionMethod(instance, processLink)
            val methodArguments = resolveMethodArguments(method, execution, processLink.actionProperties)

            logger.debug { "Invoking method ${method.name} of class ${instance.javaClass.simpleName} for activity ${execution.currentActivityId} of process-instance ${execution.processInstanceId}" }

            method.invoke(instance, *methodArguments)
        }
    }

    fun invoke(task: DelegateTask, processLink: PluginProcessLink): Any? {
        return withLoggingContext(
            mapOf(
                "com.ritense.document.domain.impl.JsonSchemaDocument" to task.execution.processBusinessKey,
                "activityId" to task.taskDefinitionKey,
                "activityName" to task.name,
                ProcessLink::class.java.canonicalName to processLink.id.toString(),
                PluginConfiguration::class.java.canonicalName to processLink.pluginConfigurationId.toString()
            )
        ) {
            val instance: Any = createInstance(processLink.pluginConfigurationId)

            val method = getActionMethod(instance, processLink)
            val methodArguments = resolveMethodArguments(method, task, processLink.actionProperties)

            logger.debug { "Invoking method ${method.name} of class ${instance.javaClass.simpleName} for task ${task.taskDefinitionKey} of process-instance ${task.processInstanceId}" }

            method.invoke(instance, *methodArguments)
        }
    }

    private fun updatePluginConfigurationId(
        oldPluginConfigurationId: PluginConfigurationId,
        newPluginConfigurationId: PluginConfigurationId
    ): PluginConfiguration {
        val oldPluginConfiguration = pluginConfigurationRepository.findById(oldPluginConfigurationId).orElseThrow()
        if (newPluginConfigurationId == oldPluginConfigurationId) {
            return oldPluginConfiguration
        }
        if (pluginConfigurationRepository.existsById(newPluginConfigurationId)) {
            throw IllegalStateException("Failed to update plugin. Plugin ID '${newPluginConfigurationId.id}' is already used by another plugin.")
        }
        pluginConfigurationRepository.deleteById(oldPluginConfigurationId)
        val newPluginConfiguration = pluginConfigurationRepository.save(
            PluginConfiguration(
                newPluginConfigurationId,
                oldPluginConfiguration.title,
                oldPluginConfiguration.properties,
                oldPluginConfiguration.pluginDefinition,
                encryptionService,
                objectMapper
            )
        )

        val event = PluginConfigurationIdUpdatedEvent(
            newPluginConfigurationId.id,
            oldPluginConfigurationId.id,
            newPluginConfiguration
        )

        applicationEventPublisher.publishEvent(event)

        return newPluginConfiguration
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
        return withLoggingContext("com.ritense.valtimo.camunda.domain.CamundaTask", task.id) {

            val actionParamValueMap = resolveActionParamValues(task, method, actionProperties)

            method.parameters.map { param ->
                actionParamValueMap[param]
                    ?: if (param.type.isInstance(task)) {
                        task
                    } else {
                        null
                    }

            }.toTypedArray()
        }
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
        val resolvedValueMap = method.parameters.filter { param ->
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

        return mapActionParamValues(paramValues, resolvedValueMap)
    }

    private fun resolveActionParamValues(
        task: DelegateTask,
        method: Method,
        actionProperties: ObjectNode?
    ): Map<Parameter, Any> {
        return withLoggingContext("com.ritense.valtimo.camunda.domain.CamundaTask", task.id) {

            if (actionProperties == null) {
                return@withLoggingContext mapOf()
            }

            val paramValues = method.parameters.filter { param ->
                param.isAnnotationPresent(PluginActionProperty::class.java)
            }.mapNotNull { param ->
                param to actionProperties.get(param.name)
            }.filter { pair ->
                pair.second != null
            }.toMap()

            // We want to process all placeholder values together to improve performance if external sources are needed.
            val resolvedValueMap = method.parameters.filter { param ->
                param.isAnnotationPresent(PluginActionProperty::class.java)
            }.mapNotNull { param ->
                param to actionProperties.get(param.name)
            }.toMap()
                .filterValues { it != null && it.isTextual }
                .mapValues {
                    it.value.textValue()
                }.run {
                    // Resolve all string values, which might or might not be placeholders.
                    valueResolverService.resolveValues(
                        task.execution.processInstanceId,
                        task.execution,
                        values.toList()
                    )
                }

            mapActionParamValues(paramValues, resolvedValueMap)
        }
    }

    private fun mapActionParamValues(
        paramValues: Map<Parameter, JsonNode>,
        resolvedValueMap: Map<String, Any?>
    ): Map<Parameter, Any> {
        return paramValues.mapValues { (param, value) ->
            if (value.isTextual) {
                if (resolvedValueMap.containsKey(value.textValue())) {
                    objectMapper.convertValue(
                        resolvedValueMap[value.textValue()],
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

    fun <T> createInstance(
        @LoggableResource(resourceType = PluginConfiguration::class) pluginConfigurationId: String
    ): T {
        return createInstance(UUID.fromString(pluginConfigurationId))
    }

    fun <T> createInstance(pluginConfigurationId: UUID): T {
        return createInstance(PluginConfigurationId.existingId(pluginConfigurationId)) as T
    }

    fun createInstance(
        @LoggableResource(resourceType = PluginConfiguration::class) pluginConfigurationId: PluginConfigurationId
    ): Any {
        val configuration = pluginConfigurationRepository.getReferenceById(pluginConfigurationId)
        return createInstance(configuration)
    }

    fun createInstance(pluginConfiguration: PluginConfiguration): Any {
        return withLoggingContext(PluginConfiguration::class, pluginConfiguration.id) {
            pluginFactories.first {
                it.canCreate(pluginConfiguration)
            }.create(pluginConfiguration)
        }
    }

    fun <T> createInstance(clazz: Class<T>, configurationFilter: (JsonNode) -> Boolean): T? {
        val pluginConfiguration = findPluginConfiguration(clazz, configurationFilter)

        return pluginConfiguration?.let { createInstance(it) as T }
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
                    validateProperty(pluginProperty, propertyNode, pluginClass)
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

    fun getPluginConfiguration(
        @LoggableResource(resourceType = PluginConfiguration::class) id: PluginConfigurationId
    ): PluginConfiguration {
        return pluginConfigurationRepository.findById(id)
            .orElseThrow { IllegalStateException("Plugin configuration with id '$id' does not exist!") }
    }

    @Throws(ConstraintViolationException::class)
    private fun validateProperty(pluginProperty: PluginProperty, propertyNode: JsonNode?, pluginClass: Class<*>) {
        val propertyClass = Class.forName(pluginProperty.fieldType)
        val propertyClassIsPlugin = propertyClass.isAnnotationPresent(Plugin::class.java)
        val propertyClassIsPluginCategory = propertyClass.isAnnotationPresent(PluginCategory::class.java)
        if (propertyClassIsPlugin || propertyClassIsPluginCategory) {
            val propertyConfigurationId =
                PluginConfigurationId.existingId(UUID.fromString(propertyNode?.textValue()))
            val propertyConfiguration = pluginConfigurationRepository.findById(propertyConfigurationId)
            require(propertyConfiguration.isPresent) { "Plugin configuration with id ${propertyConfigurationId.id} does not exist!" }
        } else {
            val propertyValue = objectMapper.treeToValue(propertyNode, propertyClass)
            val validationErrors =
                validator.validateValue(pluginClass, pluginProperty.fieldName, propertyValue)
            if (validationErrors.isNotEmpty()) {
                throw ConstraintViolationException(validationErrors)
            }
        }
    }

    fun <T> findPluginConfiguration(clazz: Class<T>, configurationFilter: (JsonNode) -> Boolean): PluginConfiguration? {
        return findPluginConfigurations(clazz, configurationFilter)
            .firstOrNull()
    }

    fun findPluginConfiguration(
        @LoggableResource(resourceType = PluginDefinition::class) pluginDefinitionKey: String,
        filter: (JsonNode) -> Boolean
    ): PluginConfiguration? {
        return findPluginConfigurations(pluginDefinitionKey, filter)
            .firstOrNull()
    }

    fun <T> findPluginConfigurations(clazz: Class<T>, filter: (JsonNode) -> Boolean = { true }): List<PluginConfiguration> {
        val annotation = clazz.getAnnotation(Plugin::class.java)
            ?: throw IllegalArgumentException("Requested plugin for class ${clazz.name}, but class is not annotated as plugin")

        return findPluginConfigurations(annotation.key, filter)
    }

    private fun findPluginConfigurations(
        pluginDefinitionKey: String,
        filter: (JsonNode) -> Boolean
    ): List<PluginConfiguration> {
        val configurations = pluginConfigurationRepository.findByPluginDefinitionKey(pluginDefinitionKey)
        return configurations.filter { config ->
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
        return pluginActionDefinitionRepository.findByActivityTypes(ActivityTypeWithEventName.fromValue(activityType))
    }

    companion object {
        val logger = KotlinLogging.logger {}

        const val PROCESS_LINK_TYPE_PLUGIN = "plugin"
    }
}

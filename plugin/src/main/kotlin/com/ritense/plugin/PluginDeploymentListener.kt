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

package com.ritense.plugin

import com.ritense.plugin.annotation.PluginProperty as PluginPropertyAnnotation
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginCategory
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginActionDefinitionId
import com.ritense.plugin.domain.PluginActionPropertyDefinition
import com.ritense.plugin.domain.PluginActionPropertyDefinitionId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.exception.PluginDefinitionNotDeployedException
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginActionPropertyDefinitionRepository
import com.ritense.plugin.repository.PluginCategoryRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter

open class PluginDeploymentListener(
    private val pluginDefinitionResolver: PluginDefinitionResolver,
    private val pluginCategoryResolver: PluginCategoryResolver,
    private val pluginDefinitionRepository: PluginDefinitionRepository,
    private val pluginCategoryRepository: PluginCategoryRepository,
    private val pluginActionDefinitionRepository: PluginActionDefinitionRepository,
    private val pluginActionPropertyDefinitionRepository: PluginActionPropertyDefinitionRepository
) {

    @Transactional
    @EventListener(ApplicationStartedEvent::class)
    open fun deployPlugins() {
        deployPluginCategories()
        deployPluginDefinitions()
    }

    private fun deployPluginCategories() {
        logger.info { "Deploying plugin categories" }

        val duplicates = mutableMapOf<String, MutableList<String>>()
        val allCategories = mutableMapOf<PluginCategory, Class<*>>()

        val pluginCategoryClasses = pluginCategoryResolver.findPluginCategoryClasses()

        pluginCategoryClasses.forEach { (clazz, category) ->
            if (!allCategories.containsKey(category)) {
                allCategories[category] = clazz
            } else {
                if (!duplicates.containsKey(category.key)) {
                    duplicates[category.key] = mutableListOf(allCategories[category]!!.name)
                }
                duplicates[category.key]?.add(clazz.name)
            }
        }

        if (duplicates.isNotEmpty()) {
            val messageBuilder = StringBuilder()
            messageBuilder.append("Found duplicate plugin categories:")
            duplicates.forEach { (categoryKey, classList) ->
                messageBuilder.append("\n - category '$categoryKey' for classes [${classList.joinToString()}]")
            }
            throw IllegalStateException(messageBuilder.toString())
        } else {
            allCategories.map {
                com.ritense.plugin.domain.PluginCategory(it.key.key, it.value.name)
            }.forEach {
                pluginCategoryRepository.save(it)
            }
        }
    }

    private fun deployPluginDefinitions() {
        logger.info { "Deploying plugins" }

        val classes = findPluginClasses()

        classes.forEach { (clazz, pluginAnnotation) ->
            try {
                val deployedPluginDefinition = createPluginDefinition(clazz, pluginAnnotation)

                createActionDefinition(deployedPluginDefinition, clazz)

            } catch (e: Exception) {
                throw PluginDefinitionNotDeployedException(pluginAnnotation.key, clazz.name, e)
            }
        }
    }

    private fun createPluginDefinition(clazz: Class<*>, pluginAnnotation: Plugin): PluginDefinition {
        val pluginDefinition = PluginDefinition(
            pluginAnnotation.key,
            pluginAnnotation.title,
            pluginAnnotation.description,
            clazz.name,
            mutableSetOf(),
            mutableSetOf()
        )

        linkCategories(pluginDefinition, clazz)
        createProperties(pluginDefinition, clazz)

        return deployPluginDefinition(pluginDefinition)
    }

    private fun linkCategories(pluginDefinition: PluginDefinition, clazz: Class<*>) {
        if (clazz.isAnnotationPresent(PluginCategory::class.java)) {
            val categoryAnnotation = clazz.getAnnotation(PluginCategory::class.java)
            val category = pluginCategoryRepository.findById(categoryAnnotation.key)
            category.map {
                pluginDefinition.addCategory(it)
            }
        }
        clazz.superclass?.let {
            linkCategories(pluginDefinition, it)
        }
        clazz.interfaces.forEach {
            linkCategories(pluginDefinition, it)
        }
    }

    private fun createProperties(
        pluginDefinition: PluginDefinition,
        clazz: Class<*>
    ) {
        val properties = findPluginProperties(clazz)
        properties.forEach { (field, propertyAnnotation) ->
            pluginDefinition.addProperty(field, propertyAnnotation)
        }
    }

    private fun createActionDefinition(deployedPluginDefinition: PluginDefinition, clazz: Class<*>) {
        findPluginActions(clazz)
            .forEach { (method, actionAnnotation) ->
                val actionDefinition = deployActionDefinition(
                    PluginActionDefinition(
                        PluginActionDefinitionId(
                            actionAnnotation.key,
                            deployedPluginDefinition
                        ),
                        actionAnnotation.title,
                        actionAnnotation.description,
                        method.name,
                        actionAnnotation.activityTypes.toList()
                    )
                )
                findPluginActionParameters(method)
                    .forEach { (parameter, _) ->
                        deployActionParameterDefinition(
                            PluginActionPropertyDefinition(
                                PluginActionPropertyDefinitionId(
                                    actionDefinition.id,
                                    parameter.name
                                )
                            )
                        )
                    }
            }
    }

    private fun findPluginClasses() : Map<Class<*>, Plugin> {
        return pluginDefinitionResolver.findPluginClasses()
    }

    private fun deployPluginDefinition(pluginDefinition: PluginDefinition): PluginDefinition {
        logger.info { "Deploying plugin ${pluginDefinition.key}" }
        logger.debug { "$pluginDefinition" }
        return pluginDefinitionRepository.save(pluginDefinition)
    }

    private fun findPluginProperties(pluginClass: Class<*>) : Map<Field, PluginPropertyAnnotation> {
        return pluginClass.declaredFields.filter { field ->
            field.isAnnotationPresent(PluginPropertyAnnotation::class.java)
        }.associateWith { field -> field.getAnnotation(PluginPropertyAnnotation::class.java) }
    }

    private fun findPluginActions(pluginClass: Class<*>) : Map<Method, PluginAction> {
        return pluginClass.methods.filter { method ->
            method.isAnnotationPresent(PluginAction::class.java)
        }.associateWith { method -> method.getAnnotation(PluginAction::class.java) }
    }

    private fun findPluginActionParameters(method:Method): Map<Parameter, PluginActionProperty> {
        return method.parameters.filter { parameter ->
            parameter.isAnnotationPresent(PluginActionProperty::class.java)
        }.associateWith { parameter -> parameter.getAnnotation(PluginActionProperty::class.java) }
    }

    private fun deployActionDefinition(pluginActionDefinition: PluginActionDefinition): PluginActionDefinition {
        logger.debug { "Deploying action ${pluginActionDefinition.id.key} for plugin ${pluginActionDefinition.id.pluginDefinition.key}" }
        return pluginActionDefinitionRepository.save(pluginActionDefinition)
    }

    private fun deployActionParameterDefinition(propertyDefinition: PluginActionPropertyDefinition) {
        logger.debug { "Deploying action property ${propertyDefinition.id.key} for action ${propertyDefinition.id.pluginActionDefinitionId.key} on plugin ${propertyDefinition.id.pluginActionDefinitionId.pluginDefinition.key}" }
        pluginActionPropertyDefinitionRepository.save(propertyDefinition)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

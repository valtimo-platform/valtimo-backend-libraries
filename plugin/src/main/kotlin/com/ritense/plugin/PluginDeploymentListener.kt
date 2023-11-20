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

import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperties
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginActionDefinitionId
import com.ritense.plugin.domain.PluginActionPropertyDefinition
import com.ritense.plugin.domain.PluginActionPropertyDefinitionId
import com.ritense.plugin.domain.PluginCategory
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.exception.PluginDefinitionNotDeployedException
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginActionPropertyDefinitionRepository
import com.ritense.plugin.repository.PluginCategoryRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import com.ritense.plugin.annotation.PluginCategory as PluginCategoryAnnotation
import com.ritense.plugin.annotation.PluginProperty as PluginPropertyAnnotation

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
        val allCategories = mutableMapOf<PluginCategoryAnnotation, Class<*>>()

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
                PluginCategory(it.key.key, it.value.name)
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
        val properties = mutableSetOf<PluginProperty>()
        val pluginDefinition = PluginDefinition(
            pluginAnnotation.key,
            pluginAnnotation.title,
            pluginAnnotation.description,
            clazz.name,
            properties,
            listCategories(clazz)
        )

        val constructorProperties = createConstructorProperties(pluginDefinition, clazz)
        if (constructorProperties.isNotEmpty()) {
            properties.addAll(constructorProperties)
        } else {
            properties.addAll(createProperties(pluginDefinition, clazz))
        }

        return deployPluginDefinition(pluginDefinition)
    }

    private fun listCategories(clazz: Class<*>) : Set<PluginCategory> {
        val pluginCategories = mutableSetOf<PluginCategory>()
        if (clazz.isAnnotationPresent(PluginCategoryAnnotation::class.java)) {
            val categoryAnnotation = clazz.getAnnotation(PluginCategoryAnnotation::class.java)
            val category = pluginCategoryRepository.findById(categoryAnnotation.key)
            category.map {
                pluginCategories.add(it)
            }
        }
        clazz.superclass?.let {
            pluginCategories.addAll(listCategories(it));
        }
        clazz.interfaces.forEach {
            pluginCategories.addAll(listCategories(it))
        }
        return pluginCategories
    }

    private fun createConstructorProperties(pluginDefinition: PluginDefinition, pluginClass: Class<*>) :Set<PluginProperty> {
        val constructors = pluginClass.kotlin.constructors
        return constructors.singleOrNull()?.let { constructor ->
            val parameters = constructor.parameters
            parameters.firstOrNull { param ->
                        param.hasAnnotation<PluginProperties>() ||
                            param.type.hasAnnotation<PluginProperties>()
                    }?.type?.let {
                        it.jvmErasure
                    }
        }?.let { clazz ->
            val propertiesConstructor = clazz.constructors.firstOrNull()
            propertiesConstructor?.parameters?.map { parameter ->
                val paramName = requireNotNull( parameter.name ) { "Could not get parameter name for constructor at index ${parameter.index}"}
                val annotation = parameter.findAnnotations(PluginPropertyAnnotation::class).firstOrNull()

                if(annotation != null) {
                    PluginProperty(
                        annotation.key,
                        pluginDefinition,
                        annotation.title,
                        annotation.required,
                        annotation.secret,
                        paramName,
                        parameter.type.javaType.typeName
                    )
                } else {
                    PluginProperty(
                        paramName,
                        pluginDefinition,
                        paramName,
                        !parameter.isOptional,
                        false,
                        paramName,
                        parameter.type.javaType.typeName
                    )
                }
            }?.toSet()
        }?: setOf()
    }

    private fun createProperties(
        pluginDefinition: PluginDefinition,
        clazz: Class<*>
    ) : Set<PluginProperty> {
        return clazz.declaredFields.filter { field ->
            field.isAnnotationPresent(PluginPropertyAnnotation::class.java)
        }.map {field ->
            val annotation = field.getAnnotation(PluginPropertyAnnotation::class.java)

            PluginProperty(
                annotation.key,
                pluginDefinition,
                annotation.title,
                annotation.required,
                annotation.secret,
                field.name,
                field.type.typeName
            )
        }.toSet()
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

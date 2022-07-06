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

package com.ritense.plugin

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginProperty
import org.apache.commons.lang3.reflect.FieldUtils

/**
 *
 */
abstract class PluginFactory<T> {
    private var fullyQualifiedClassName: String = ""

    protected abstract fun create(): T

    fun create(configuration: PluginConfiguration): T {
        val instance = create()

        injectProperties(instance, configuration)

        return instance
    }

    fun canCreate(configuration: PluginConfiguration): Boolean {
        if (fullyQualifiedClassName.isEmpty()) {
            val instance = create()
            fullyQualifiedClassName = instance!!::class.java.name
        }

        return this.fullyQualifiedClassName == configuration.pluginDefinition.fullyQualifiedClassName
    }

    private fun injectProperties(instance: T, configuration: PluginConfiguration) {
        if (configuration.properties == null) {
            return
        }

        val pluginDefinition = configuration.pluginDefinition
        val mapper = ObjectMapper()
        val propertyIterator = configuration.properties.fields()

        while (propertyIterator.hasNext()) {
            val configuredPropertyEntry = propertyIterator.next()

            if (configuredPropertyEntry.value.isNull) {
                continue
            }

            val propertyDefinition = pluginDefinition.findPluginProperty(configuredPropertyEntry.key)

            setProperty(instance, propertyDefinition!!, configuredPropertyEntry.value, mapper)
        }
    }

    private fun setProperty(instance: T,
        propertyDefinition: PluginProperty,
        configuredProperty: JsonNode,
        mapper: ObjectMapper
    ) {
        val propertyValue = mapper.treeToValue(
            configuredProperty,
            Class.forName(propertyDefinition.fieldType)
        )

        FieldUtils.writeField(instance, propertyDefinition.fieldName, propertyValue, true)
    }
}
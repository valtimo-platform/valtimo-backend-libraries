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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.plugin.domain.PluginConfiguration
import mu.KotlinLogging
import javax.persistence.PostLoad
import javax.persistence.PostPersist
import javax.persistence.PostUpdate
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

class PluginConfigurationEntityListener(
    val encryptionService: EncryptionService,
    val objectMapper: ObjectMapper
) {
    @PrePersist
    @PreUpdate
    fun encryptPropertiesOnSave(pluginConfiguration: PluginConfiguration) {
        logger.debug { "Encrypting secrets for PluginConfiguration ${pluginConfiguration.title}" }
        pluginConfiguration.pluginDefinition.pluginProperties.filter {
            it.secret
        }.filter {
            pluginConfiguration.properties?.has(it.fieldName) ?: false
        }.forEach {
            logger.debug { "Encrypting property ${it.fieldName} for PluginConfiguration ${pluginConfiguration.title}" }
            replaceProperty(
                pluginConfiguration.properties!!,
                it.fieldName,
                this::encryptProperty
            )
        }
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    fun decryptPropertiesOnLoad(pluginConfiguration: PluginConfiguration) {
        logger.debug { "Decrypting secrets for PluginConfiguration ${pluginConfiguration.title}" }
        pluginConfiguration.pluginDefinition.pluginProperties.filter {
            it.secret
        }.filter {
            pluginConfiguration.properties?.has(it.fieldName) ?: false
        }.forEach {
            logger.debug { "Decrypting property ${it.fieldName} for PluginConfiguration ${pluginConfiguration.title}" }
            replaceProperty(
                pluginConfiguration.properties!!,
                it.fieldName,
                this::decryptProperty
            )
        }
    }

    private fun encryptProperty(property: JsonNode): JsonNode {
        val serializedValue = objectMapper.writeValueAsString(property)
        val encryptedValue = encryptionService.encrypt(serializedValue)
        return TextNode(encryptedValue)
    }

    private fun decryptProperty(property: JsonNode): JsonNode {
        val serializedValue = property.textValue()
        val decryptedValue = encryptionService.decrypt(serializedValue)
        return objectMapper.readTree(decryptedValue)
    }

    private fun replaceProperty(properties: JsonNode, propertyName: String,  transformFun: (JsonNode) -> JsonNode) {
        if (properties is ObjectNode) {
            val originalValue = properties.get(propertyName)
            val newValue = transformFun(originalValue)
            properties.replace(propertyName, newValue)
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
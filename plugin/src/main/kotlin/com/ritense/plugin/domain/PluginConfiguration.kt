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

package com.ritense.plugin.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.plugin.service.EncryptionService
import com.ritense.plugin.service.PluginConfigurationEntityListener
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@EntityListeners(PluginConfigurationEntityListener::class)
@Table(name = "plugin_configuration")
class PluginConfiguration(
    @Id
    @Embedded
    val id: PluginConfigurationId,
    @Column(name = "title")
    var title: String,
    properties: ObjectNode? = null,
    @JoinColumn(name = "plugin_definition_key", updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    val pluginDefinition: PluginDefinition,
    @Transient
    var encryptionService: EncryptionService? = null,
    @Transient
    var objectMapper: ObjectMapper? = null,
) {
    // used to store unencrypted properties without dirtying the managed entity
    @Type(value = JsonType::class)
    @Column(name = "properties", columnDefinition = "JSON")
    internal var rawProperties: ObjectNode? = null

    @Transient
    var properties = properties
        get() {
            if (field == null && rawProperties != null)
            {
                decryptProperties()
            }
            return field
        }
        private set(value) {
            field = value
        }

    init {
        if (encryptionService != null && objectMapper != null) {
            encryptProperties()
        }
    }

    fun updateProperties(propertiesForUpdate: ObjectNode) {
        pluginDefinition.properties.forEach {
            val updateValue = propertiesForUpdate.get(it.fieldName)
            if (!it.secret || !nodeIsEmpty(updateValue)) {
                properties?.replace(it.fieldName, updateValue)
            }
        }
        encryptProperties()
    }

    internal fun encryptProperties() {
        this.rawProperties = this.properties?.deepCopy()
        PluginConfigurationEntityListener.logger.debug { "Encrypting secrets for PluginConfiguration $title" }
        pluginDefinition.properties.filter {
            it.secret
        }.filter {
            this.rawProperties?.has(it.fieldName) ?: false
        }.forEach {
            PluginConfigurationEntityListener.logger.debug { "Encrypting property ${it.fieldName} for PluginConfiguration $title" }
            replaceProperty(
                this.rawProperties!!,
                it.fieldName,
                this::encryptProperty
            )
        }
    }

    internal fun decryptProperties() {
        this.properties = this.rawProperties?.deepCopy()
        PluginConfigurationEntityListener.logger.debug { "Decrypting secrets for PluginConfiguration $title" }
        pluginDefinition.properties.filter {
            it.secret
        }.filter {
            properties?.has(it.fieldName) ?: false
        }.forEach {
            PluginConfigurationEntityListener.logger.debug { "Decrypting property ${it.fieldName} for PluginConfiguration $title" }
            replaceProperty(
                properties!!,
                it.fieldName,
                this::decryptProperty
            )
        }
    }

    private fun encryptProperty(property: JsonNode): JsonNode {
        val serializedValue = objectMapper!!.writeValueAsString(property)
        val encryptedValue = encryptionService!!.encrypt(serializedValue)
        return TextNode(encryptedValue)
    }

    private fun decryptProperty(property: JsonNode): JsonNode {
        val serializedValue = property.textValue()
        val decryptedValue = encryptionService!!.decrypt(serializedValue)
        return objectMapper!!.readTree(decryptedValue)
    }

    private fun replaceProperty(properties: ObjectNode, propertyName: String,  transformFun: (JsonNode) -> JsonNode) {
        val originalValue = properties.get(propertyName)
        if (originalValue != null && !originalValue.isNull) {
            val newValue = transformFun(originalValue)
            properties.replace(propertyName, newValue)
        }
    }

    private fun nodeIsEmpty(node: JsonNode?): Boolean {
        return node == null || node.isNull ||
            (node is TextNode && node.textValue() == "")
    }
}

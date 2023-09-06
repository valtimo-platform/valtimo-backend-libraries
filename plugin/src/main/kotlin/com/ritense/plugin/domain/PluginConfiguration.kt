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

package com.ritense.plugin.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.plugin.service.EncryptionService
import com.ritense.plugin.service.PluginConfigurationEntityListener
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@EntityListeners(PluginConfigurationEntityListener::class)
@Table(name = "plugin_configuration")
class PluginConfiguration(
    @Id
    @Embedded
    val id: PluginConfigurationId,
    @Column(name = "title")
    var title: String,
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "properties", columnDefinition = "JSON")
    internal var rawProperties: ObjectNode? = null,
    @JoinColumn(name = "plugin_definition_key", updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    val pluginDefinition: PluginDefinition,
) {
    // used to store unencrypted properties without dirtying the managed entity
    @Transient
    var properties: ObjectNode? = rawProperties?.deepCopy()
        get() {
            if (field == null && rawProperties != null) {
                field = this.rawProperties?.deepCopy()
                decryptProperties()
            }
            return field
        }
    @Transient
    var encryptionService: EncryptionService? = null
    @Transient
    var objectMapper: ObjectMapper? = null

    fun updateProperties(propertiesForUpdate: ObjectNode) {
        pluginDefinition.properties.forEach {
            val updateValue = propertiesForUpdate.get(it.fieldName)
            if (!it.secret || !nodeIsEmpty(updateValue)) {
                properties?.replace(it.fieldName, updateValue)
            }
        }
        encryptProperties(properties)
    }

    internal fun encryptProperties() {
        encryptProperties(this.rawProperties)
    }

    private fun encryptProperties(propertiesToEncrypt: ObjectNode?) {
        if (propertiesToEncrypt !== this.rawProperties) {
            this.rawProperties = propertiesToEncrypt?.deepCopy()
        }
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

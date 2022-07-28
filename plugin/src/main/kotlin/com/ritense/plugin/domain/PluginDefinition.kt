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

package com.ritense.plugin.domain

import com.ritense.plugin.annotation.PluginProperty as PluginPropertyAnnotation
import com.fasterxml.jackson.annotation.JsonIgnore
import java.lang.reflect.Field
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "plugin_definition")
data class PluginDefinition (
    @Id
    @Column(name = "plugin_definition_key")
    val key: String,
    @Column(name = "title")
    val title: String,
    @Column(name = "description")
    val description: String,
    @JsonIgnore
    @Column(name = "class_name")
    val fullyQualifiedClassName: String,
    @JsonIgnore@OneToMany(mappedBy = "pluginDefinition", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val pluginProperties: Set<PluginProperty> = setOf(),
) {
    fun findPluginProperty(propertyKey: String): PluginProperty? {
        val filteredProperties = pluginProperties.filter {
            it.id.key == propertyKey
        }

        return if (filteredProperties.size == 1) {
            filteredProperties[0]
        } else {
            null
        }
    }

    fun addProperty(field: Field, propertyAnnotation: PluginPropertyAnnotation) {
        (pluginProperties as MutableSet).add(
            PluginProperty(
                propertyAnnotation.key,
                this,
                propertyAnnotation.title,
                propertyAnnotation.required,
                propertyAnnotation.secret,
                field.name,
                field.type.typeName
            )
        )
    }
}
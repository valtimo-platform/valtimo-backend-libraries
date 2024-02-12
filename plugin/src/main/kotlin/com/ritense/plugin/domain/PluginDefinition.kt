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

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.lang.reflect.Field
import com.ritense.plugin.annotation.PluginProperty as PluginPropertyAnnotation

@Entity
@Table(name = "plugin_definition")
class PluginDefinition (
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
    @JsonIgnore
    @OneToMany(mappedBy = "pluginDefinition", fetch = FetchType.EAGER, cascade = [CascadeType.ALL],
        orphanRemoval = true)
    val properties: Set<PluginProperty> = setOf(),
    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "plugin_definition_category",
        joinColumns = [JoinColumn(name = "plugin_definition_key")],
        inverseJoinColumns = [JoinColumn(name = "plugin_category_key")])
    val categories: Set<PluginCategory> = setOf(),
    @JsonIgnore
    @OneToMany(mappedBy = "id.pluginDefinition", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE],
        orphanRemoval = true)
    val actions: Set<PluginActionDefinition> = setOf(),
) {
    fun findPluginProperty(propertyKey: String): PluginProperty? {
        val filteredProperties = properties.filter {
            it.id.key == propertyKey
        }

        return if (filteredProperties.size == 1) {
            filteredProperties[0]
        } else {
            null
        }
    }

    fun addProperty(field: Field, propertyAnnotation: PluginPropertyAnnotation) {
        (properties as MutableSet).add(
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

    fun addCategory(category: PluginCategory) {
        (categories as MutableSet).add(category)
    }
}
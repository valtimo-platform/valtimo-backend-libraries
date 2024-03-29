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

import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.Table

@Entity
@Table(name = "plugin_property")
class PluginProperty(
    key: String,
    @ManyToOne(targetEntity = PluginDefinition::class, fetch = FetchType.LAZY)
    @MapsId("pluginDefinitionId")
    @JoinColumns(
        JoinColumn(name = "plugin_definition_key", referencedColumnName = "plugin_definition_key"),
    )
    var pluginDefinition: PluginDefinition,
    @Column(name = "title")
    val title: String,
    @Column(name = "required")
    val required: Boolean,
    @Column(name = "secret")
    val secret: Boolean,
    @Column(name = "field_name")
    val fieldName: String,
    @Column(name = "field_type")
    val fieldType: String
){
    @EmbeddedId
    val id: PluginPropertyId

    init {
        id = PluginPropertyId(key, pluginDefinition.key)
    }
}
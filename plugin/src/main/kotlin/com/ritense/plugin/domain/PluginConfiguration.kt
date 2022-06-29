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

import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.valtimo.contract.json.Mapper
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "plugin_configuration")
class PluginConfiguration(
    @Id
    @Column(name = "plugin_configuration_key")
    val key: String,
    @Column(name = "title")
    val title: String,
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "properties", columnDefinition = "JSON")
    val properties: String? = null,
    @JoinColumn(name = "plugin_definition_key", updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val pluginDefinition: PluginDefinition,
) {
    inline fun <reified T> getProperties(): T {
        return if (properties == null) {
            throw IllegalStateException("No properties found for plugin configuration $key")
        } else {
            Mapper.INSTANCE.get().readValue(properties)
        }
    }
}

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

import com.fasterxml.jackson.databind.node.ObjectNode
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "plugin_process_link")
data class PluginProcessLink(
    @Id
    @Embedded
    val id: PluginProcessLinkId,
    @Column(name = "process_definition_id", updatable = false)
    val processDefinitionId: String,
    @Column(name = "activity_id", updatable = false)
    val activityId: String,
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "action_properties", columnDefinition = "JSON")
    val actionProperties: ObjectNode? = null,
    @Embedded
    val pluginConfigurationId: PluginConfigurationId,
    @Column(name = "plugin_action_definition_key")
    val pluginActionDefinitionKey: String
)
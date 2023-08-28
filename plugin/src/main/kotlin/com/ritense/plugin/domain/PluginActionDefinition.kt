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

import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.JoinColumn
import javax.persistence.Table

@Entity
@Table(name = "plugin_action_definition")
class PluginActionDefinition(
    @EmbeddedId
    @Column(name = "plugin_action_definition_key")
    val id: PluginActionDefinitionId,
    @Column(name = "title")
    val title: String,
    @Column(name = "description")
    val description: String,
    @Column(name = "method_name")
    val methodName: String,
    @ElementCollection
    @CollectionTable(name = "plugin_action_definition_activity", joinColumns = [
        JoinColumn(name = "plugin_action_definition_key", referencedColumnName = "plugin_action_definition_key"),
        JoinColumn(name = "plugin_definition_key", referencedColumnName = "plugin_definition_key")
    ])
    @Column(name = "activity_type")
    @Enumerated(EnumType.STRING)
    val activityTypes: Collection<ActivityType>
)
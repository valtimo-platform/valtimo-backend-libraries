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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.service.PluginService.Companion.PROCESS_LINK_TYPE_PLUGIN
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Embedded
import javax.persistence.Entity

@Entity
@DiscriminatorValue(PROCESS_LINK_TYPE_PLUGIN)
data class PluginProcessLink(
    override val id: UUID,

    override val processDefinitionId: String,

    override val activityId: String,

    override val activityType: ActivityTypeWithEventName,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "action_properties", columnDefinition = "JSON")
    val actionProperties: ObjectNode? = null,

    @Embedded
    val pluginConfigurationId: PluginConfigurationId,

    @Column(name = "plugin_action_definition_key", nullable = false)
    val pluginActionDefinitionKey: String

) : ProcessLink(
    id,
    processDefinitionId,
    activityId,
    activityType,
    PROCESS_LINK_TYPE_PLUGIN,
) {

    @Deprecated("Marked for removal since 10.6.0")
    constructor(
        id: PluginProcessLinkId,
        processDefinitionId: String,
        activityId: String,
        actionProperties: ObjectNode? = null,
        pluginConfigurationId: PluginConfigurationId,
        pluginActionDefinitionKey: String,
        activityType: ActivityType
    ) : this(
        id.id,
        processDefinitionId,
        activityId,
        activityType.toActivityTypeWithEventName(),
        actionProperties,
        pluginConfigurationId,
        pluginActionDefinitionKey,
    )

    override fun copy(
        id: UUID,
        processDefinitionId: String,
    ) = copy(
        id = id,
        processDefinitionId = processDefinitionId,
        activityId = activityId,
        activityType = activityType,
        actionProperties = actionProperties,
        pluginConfigurationId = pluginConfigurationId,
        pluginActionDefinitionKey = pluginActionDefinitionKey
    )
}

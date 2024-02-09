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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.service.PluginService.Companion.PROCESS_LINK_TYPE_PLUGIN
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import org.hibernate.annotations.Type
import java.util.UUID

@Entity
@DiscriminatorValue(PROCESS_LINK_TYPE_PLUGIN)
class PluginProcessLink(
    id: UUID,
    processDefinitionId: String,
    activityId: String,
    activityType: ActivityTypeWithEventName,

    @Type(value = JsonType::class)
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
        activityType: ActivityTypeWithEventName
    ) : this(
        id.id,
        processDefinitionId,
        activityId,
        activityType,
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
        activityId = activityId
    )

    fun copy(
        id: UUID = this.id,
        processDefinitionId: String = this.processDefinitionId,
        activityId: String = this.activityId,
        activityType: ActivityTypeWithEventName = this.activityType,
        actionProperties: ObjectNode? = this.actionProperties,
        pluginConfigurationId: PluginConfigurationId = this.pluginConfigurationId,
        pluginActionDefinitionKey: String = this.pluginActionDefinitionKey,
    ) = PluginProcessLink(
        id = id,
        processDefinitionId = processDefinitionId,
        activityId = activityId,
        activityType = activityType,
        actionProperties = actionProperties,
        pluginConfigurationId = pluginConfigurationId,
        pluginActionDefinitionKey = pluginActionDefinitionKey
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PluginProcessLink

        if (actionProperties != other.actionProperties) return false
        if (pluginConfigurationId != other.pluginConfigurationId) return false
        if (pluginActionDefinitionKey != other.pluginActionDefinitionKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (actionProperties?.hashCode() ?: 0)
        result = 31 * result + pluginConfigurationId.hashCode()
        result = 31 * result + pluginActionDefinitionKey.hashCode()
        return result
    }
}

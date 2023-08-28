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

package com.ritense.plugin.repository

import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.processlink.domain.ActivityTypeWithEventName
import org.springframework.data.jpa.repository.JpaRepository

@Deprecated("Marked for removal since 10.6.0", ReplaceWith("ProcessLinkRepository"))
class PluginProcessLinkRepository(
    private val pluginProcessLinkRepositoryImpl: PluginProcessLinkRepositoryImpl
) {
    fun getById(id: PluginProcessLinkId) = pluginProcessLinkRepositoryImpl.getById(id)
    fun save(entity: PluginProcessLink) = pluginProcessLinkRepositoryImpl.save(entity)
    fun deleteById(id: PluginProcessLinkId) = pluginProcessLinkRepositoryImpl.deleteById(id)
    fun findByProcessDefinitionId(processDefinitionId: String) =
        pluginProcessLinkRepositoryImpl.findByProcessDefinitionId(processDefinitionId)

    fun findByProcessDefinitionIdAndActivityId(processDefinitionId: String, activityId: String) =
        pluginProcessLinkRepositoryImpl.findByProcessDefinitionIdAndActivityId(processDefinitionId, activityId)

    fun findByProcessDefinitionIdAndActivityIdAndActivityType(processDefinitionId: String, activityId: String, activityType: ActivityType) =
        pluginProcessLinkRepositoryImpl.findByProcessDefinitionIdAndActivityIdAndActivityType(processDefinitionId, activityId, activityType.toActivityTypeWithEventName())

    fun findByPluginConfigurationIdAndActivityIdAndActivityType(pluginConfigurationId: PluginConfigurationId, activityId: String, activityType: ActivityType) =
        pluginProcessLinkRepositoryImpl.findByPluginConfigurationIdAndActivityIdAndActivityType(pluginConfigurationId, activityId, activityType.toActivityTypeWithEventName())
}

@Deprecated("Marked for removal since 10.6.0", ReplaceWith("ProcessLinkRepository"))
interface PluginProcessLinkRepositoryImpl : JpaRepository<PluginProcessLink, PluginProcessLinkId> {
    fun findByProcessDefinitionId(processDefinitionId: String): List<PluginProcessLink>
    fun findByProcessDefinitionIdAndActivityId(processDefinitionId: String, activityId: String): List<PluginProcessLink>
    fun findByProcessDefinitionIdAndActivityIdAndActivityType(processDefinitionId: String, activityId: String, activityType: ActivityTypeWithEventName): List<PluginProcessLink>
    fun findByPluginConfigurationIdAndActivityIdAndActivityType(pluginConfigurationId: PluginConfigurationId, activityId: String, activityType: ActivityTypeWithEventName): List<PluginProcessLink>
}

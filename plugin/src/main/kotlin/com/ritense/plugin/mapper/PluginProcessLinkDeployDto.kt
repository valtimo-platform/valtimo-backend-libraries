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

package com.ritense.plugin.mapper

import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.service.PluginService.Companion.PROCESS_LINK_TYPE_PLUGIN
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ActivityTypeWithEventName
import java.util.UUID

@JsonTypeName(PROCESS_LINK_TYPE_PLUGIN)
class PluginProcessLinkDeployDto(
    override val processDefinitionId: String,
    override val activityId: String,
    override val activityType: ActivityTypeWithEventName,
    val pluginConfigurationId: UUID,
    val pluginActionDefinitionKey: String,
    val actionProperties: ObjectNode? = JsonNodeFactory.instance.objectNode(),
) : ProcessLinkDeployDto {
    override val processLinkType: String
        get() = PROCESS_LINK_TYPE_PLUGIN
}
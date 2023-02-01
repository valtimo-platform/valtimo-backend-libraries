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

package com.ritense.portaaltaak

import com.ritense.notificatiesapi.NotificatiesApiPlugin
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import java.lang.IllegalStateException
import java.util.UUID

@Plugin(
    key = "portaaltaak",
    title = "Portaaltaak",
    description = "Enable interfacing with Portaaltaak specification compliant APIs"
)
class PortaaltaakPlugin(
    private val objectManagementService: ObjectManagementService,
    private val pluginService: PluginService
) {

    @PluginProperty(key = "notificatiesApiPluginConfiguration", secret = false)
    lateinit var notificatiesApiPluginConfiguration: NotificatiesApiPlugin

    @PluginProperty(key = "objectManagementConfigurationId", secret = false)
    lateinit var objectManagementConfigurationId: UUID

    @PluginAction(
        key = "create-portaaltaak",
        title = "Create portal task",
        description = "Create a task for a portal by storing it in the Objecten-API",
        activityTypes = [ActivityType.USER_TASK]
    )
    fun createPortaalTaak(
        @PluginActionProperty formType: TaakFormType,
        @PluginActionProperty formTypeId: String?,
        @PluginActionProperty formTypeUrl: String?,
        @PluginActionProperty sendData: List<DataBindingConfig>,
        @PluginActionProperty receiveData: List<DataBindingConfig>,
        @PluginActionProperty receiver: TaakReceiver,
        @PluginActionProperty otherReceiver: String?,
        @PluginActionProperty kvk: String?,
        @PluginActionProperty bsn: String?
    ) {
        val objectManagement = objectManagementService.getById(objectManagementConfigurationId)
            ?: throw IllegalStateException("Object management not found for portaal taak")

        val objectenApiPlugin = pluginService.createInstance(PluginConfigurationId
            .existingId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin

        val portaalTaak = TaakObject(
            listOf(getIdentification())
        )

        //objectenApiPlugin.create
    }

    private fun getIdentification(): TaakIndentificatie {
        TODO("Not yet implemented")
    }

    private fun getTaakData(sendData: List<DataBindingConfig>): Map<String, Any> {
        return mapOf()
    }
}

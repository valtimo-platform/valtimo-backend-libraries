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

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.delegate.DelegateTask
import java.util.UUID

@Plugin(`
    key = "portaaltaak",
    title = "Portaaltaak",
    description = "Enable interfacing with Portaaltaak specification compliant APIs"
)
class PortaaltaakPlugin(
    private val objectManagementService: ObjectManagementService,
    private val pluginService: PluginService,
    private val valueResolverService: ValueResolverService,
    private val processDocumentService: ProcessDocumentService,
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
        delegateTask: DelegateTask,
        @PluginActionProperty formType: TaakFormType,
        @PluginActionProperty formTypeId: String?,
        @PluginActionProperty formTypeUrl: String?,
        @PluginActionProperty sendData: List<DataBindingConfig>,
        @PluginActionProperty receiveData: List<DataBindingConfig>,
        @PluginActionProperty receiver: TaakReceiver,
        @PluginActionProperty otherReceiver: OtherTaakReceiver?,
        @PluginActionProperty kvk: String?,
        @PluginActionProperty bsn: String?
    ) {
        val objectManagement = objectManagementService.getById(objectManagementConfigurationId)
            ?: throw IllegalStateException("Object management not found for portaal taak")

        val objectenApiPlugin = pluginService.createInstance(
            PluginConfigurationId
                .existingId(objectManagement.objectenApiPluginConfigurationId)
        ) as ObjectenApiPlugin

        val portaalTaak = TaakObject(
            listOf(getTaakIdentification(receiver, otherReceiver, kvk, bsn)),
            getTaakData(delegateTask, sendData),
            delegateTask.name,
            TaakStatus.OPEN,
            getTaakForm(),
            delegateTask.id
        )

        //TODO: create eactual object
        //objectenApiPlugin.create
    }

    private fun getTaakIdentification(
        receiver: TaakReceiver,
        otherReceiver: OtherTaakReceiver?,
        kvk: String?,
        bsn: String?
    ): TaakIdentificatie {
        when (receiver){
            TaakReceiver.ZAAK_INITIATOR -> getZaakinitiator()
            TaakReceiver.OTHER -> {
                val identificationValue = when (otherReceiver) {
                    OtherTaakReceiver.BSN -> bsn
                    OtherTaakReceiver.KVK -> kvk
                    null ->  throw IllegalStateException("Other was chosen as taak receiver, but no identification type was chosen.")
                }?: throw IllegalStateException("Could not find identification value in configuration for type ${otherReceiver.key}")

                TaakIdentificatie(
                    otherReceiver.key,
                    identificationValue
                )
            }
        }
    }

    private fun getZaakinitiator(): TaakIdentificatie {
        //TODO: this method
        //get zaak link by not using the openzaak module

        val zakenPlugin = pluginService.createInstance() as ZakenApiPlugin

        //get all zaakrollen for zaak
        //zakenPlugin.getZaakRollen

        //get zaakrol with type iniator

        //build
    }

    private fun getTaakForm(): TaakForm {
        //TODO: not specified in story
    }

    private fun getTaakData(delegateTask: DelegateTask, sendData: List<DataBindingConfig>): Map<String, Any> {
        val processInstanceId = CamundaProcessInstanceId(delegateTask.processInstanceId)
        val documentId = processDocumentService.getDocumentId(processInstanceId, delegateTask).toString()
        val sendDataValuesResolvedMap = valueResolverService.resolveValues(documentId, sendData.map { it.value })

        if (sendData.size != sendDataValuesResolvedMap.size) {
            val failedValues = sendData
                .filter { sendDataValuesResolvedMap.containsKey(it.value) }
                .joinToString(", ") { "'${it.key}' = '${it.value}'" }
            throw IllegalArgumentException(
                """
                    Error in sendData for task: '${delegateTask.taskDefinitionKey}' and documentId: '${documentId}'. Failed to resolve values:
                    $failedValues
                """.trimMargin()
            )
        }

        val sendDataResolvedMap = sendData.associate { it.key to sendDataValuesResolvedMap[it.value] }
        val jsonPatchBuilder = JsonPatchBuilder()
        val taakData = jacksonObjectMapper().createObjectNode()

        sendDataResolvedMap.forEach {
            val path = JsonPointer.valueOf(it.key)
            val valueNode = jacksonObjectMapper().valueToTree<JsonNode>(it.value)
            jsonPatchBuilder.addJsonNodeValue(taakData, path, valueNode)
        }

        return jacksonObjectMapper().convertValue(taakData)
    }
}

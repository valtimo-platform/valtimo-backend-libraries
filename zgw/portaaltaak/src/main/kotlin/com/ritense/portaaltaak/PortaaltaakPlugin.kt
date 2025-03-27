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

package com.ritense.portaaltaak

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.logging.withLoggingContext
import com.ritense.notificatiesapi.NotificatiesApiPlugin
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.portaaltaak.exception.CompleteTaakProcessVariableNotFoundException
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import mu.KLogger
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Plugin(
    key = "portaaltaak",
    title = "Portaaltaak",
    description = "Enable interfacing with Portaaltaak specification compliant APIs"
)
class PortaaltaakPlugin(
    private val objectManagementService: ObjectManagementService,
    private val pluginService: PluginService,
    private val valueResolverService: ValueResolverService,
    private val processDocumentService: ProcessDocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val taskService: CamundaTaskService
) {
    private val objectMapper = pluginService.getObjectMapper()

    @PluginProperty(key = "notificatiesApiPluginConfiguration", secret = false)
    lateinit var notificatiesApiPluginConfiguration: NotificatiesApiPlugin

    @PluginProperty(key = "objectManagementConfigurationId", secret = false)
    lateinit var objectManagementConfigurationId: UUID

    @PluginProperty(key = "completeTaakProcess", secret = false)
    lateinit var completeTaakProcess: String

    @PluginAction(
        key = "create-portaaltaak",
        title = "Create portal task",
        description = "Create a task for a portal by storing it in the Objecten-API",
        activityTypes = [ActivityTypeWithEventName.USER_TASK_CREATE]
    )
    fun createPortaalTaak(
        delegateTask: DelegateTask,
        @PluginActionProperty formType: TaakFormType,
        @PluginActionProperty formTypeId: String?,
        @PluginActionProperty formTypeUrl: String?,
        @PluginActionProperty sendData: List<DataBindingConfig>,
        @PluginActionProperty receiveData: List<DataBindingConfig>,
        @PluginActionProperty receiver: TaakReceiver,
        @PluginActionProperty identificationKey: String?,
        @PluginActionProperty identificationValue: String?,
        @PluginActionProperty verloopDurationInDays: Long?,
    ) {
        withLoggingContext(DelegateTask::class.java.canonicalName to delegateTask.id) {
            logger.debug { "Creating portaaltaak for task with id '${delegateTask.id}'" }

            val objectManagement = objectManagementService.getById(objectManagementConfigurationId)
                ?: throw IllegalStateException("Object management not found for portaaltaak")

            val objectenApiPlugin = pluginService.createInstance(
                PluginConfigurationId
                    .existingId(objectManagement.objectenApiPluginConfigurationId)
            ) as ObjectenApiPlugin

            val processInstanceId = CamundaProcessInstanceId(delegateTask.processInstanceId)
            val documentId = processDocumentService.getDocumentId(processInstanceId, delegateTask).id

            val zaakUrl = try {
                zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl
            } catch (e: ZaakInstanceLinkNotFoundException) {
                // this should set zaakUrl to null when no zaak has been linked for this case
                null
            }

            val verloopdatum = verloopDurationInDays?.let { LocalDateTime.now().plusDays(verloopDurationInDays) }
                ?: delegateTask.dueDate?.let {
                    LocalDateTime.ofInstant(
                        delegateTask.dueDate.toInstant(),
                        ZoneId.systemDefault()
                    )
                }

            val portaalTaak = TaakObject(
                getTaakIdentification(delegateTask, receiver, identificationKey, identificationValue),
                getTaakData(delegateTask, sendData, documentId.toString()),
                delegateTask.name,
                TaakStatus.OPEN,
                getTaakForm(formType, formTypeId, formTypeUrl),
                delegateTask.id,
                zaakUrl,
                verloopdatum
            )

            val objecttypenApiPlugin = pluginService
                .createInstance(PluginConfigurationId(objectManagement.objecttypenApiPluginConfigurationId)) as ObjecttypenApiPlugin
            val objectTypeUrl = objecttypenApiPlugin.getObjectTypeUrlById(objectManagement.objecttypeId)

            val createObjectRequest = ObjectRequest(
                objectTypeUrl,
                ObjectRecord(
                    typeVersion = objectManagement.objecttypeVersion,
                    data = pluginService.getObjectMapper().convertValue(portaalTaak),
                    startAt = LocalDate.now()
                )
            )

            val portalTaskObject = objectenApiPlugin.createObject(createObjectRequest)

            logger.info { "Portaaltaak object with UUID '${portalTaskObject.uuid}' and URL '${portalTaskObject.url}' created for task with id '${delegateTask.id}'" }
        }
    }

    @PluginAction(
        key = "complete-portaaltaak",
        title = "Complete Portaaltaak",
        description = "Complete portal task and update status on Objects Api",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun completePortaalTaak(delegateExecution: DelegateExecution) {
        logger.debug { "Completing portaaltaak" }

        val verwerkerTaakId = (delegateExecution.getVariable("verwerkerTaakId")
            ?: throw CompleteTaakProcessVariableNotFoundException("verwerkerTaakId is required but was not provided")) as String
        val objectenApiPluginId = (delegateExecution.getVariable("objectenApiPluginConfigurationId")
            ?: throw CompleteTaakProcessVariableNotFoundException("objectenApiPluginConfigurationId is required but was not provided")) as String
        val portaalTaakObjectUrl = URI(
            (delegateExecution.getVariable("portaalTaakObjectUrl")
                ?: throw CompleteTaakProcessVariableNotFoundException("portaalTaakObjectUrl is required but was not provided")) as String
        )

        runWithoutAuthorization { taskService.complete(verwerkerTaakId) }

        logger.info { "Task with id '${verwerkerTaakId}' for object with URL '${portaalTaakObjectUrl}' completed" }

        val objectenApiPlugin =
            pluginService.createInstance(PluginConfigurationId(UUID.fromString(objectenApiPluginId))) as ObjectenApiPlugin
        val portaalTaakMetaDataObject = objectenApiPlugin.getObject(portaalTaakObjectUrl)
        var taakObject: TaakObject = objectMapper
            .convertValue(
                portaalTaakMetaDataObject.record.data ?: throw RuntimeException("Portaaltaak meta data was empty!")
            )
        taakObject = changeStatus(taakObject, TaakStatus.VERWERKT)
        val portaalTaakMetaObjectUpdated =
            changeDataInPortalTaakObject(portaalTaakMetaDataObject, objectMapper.convertValue(taakObject))
        objectenApiPlugin.objectPatch(portaalTaakObjectUrl, portaalTaakMetaObjectUpdated)

        logger.info { "Portaaltaak object with URL '${portaalTaakObjectUrl}' completed by changing status to 'verwerkt'" }
    }

    internal fun getTaakIdentification(
        delegateTask: DelegateTask,
        receiver: TaakReceiver,
        identificationKey: String?,
        identificationValue: String?,
    ): TaakIdentificatie {
        return when (receiver) {
            TaakReceiver.ZAAK_INITIATOR -> getZaakinitiator(delegateTask)
            TaakReceiver.OTHER -> {
                if (identificationKey == null) {
                    throw IllegalStateException("Other was chosen as taak receiver, but no identification key was chosen.")
                }
                if (identificationValue == null) {
                    throw IllegalStateException("Other was chosen as taak receiver, but no identification value was chosen.")
                }
                TaakIdentificatie(
                    identificationKey,
                    identificationValue
                )
            }
        }
    }

    internal fun getZaakinitiator(delegateTask: DelegateTask): TaakIdentificatie {
        val processInstanceId = CamundaProcessInstanceId(delegateTask.processInstanceId)
        val documentId = processDocumentService.getDocumentId(processInstanceId, delegateTask)

        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId.id).zaakInstanceUrl
        val zakenPlugin = requireNotNull(
            pluginService.createInstance(ZakenApiPlugin::class.java, ZakenApiPlugin.findConfigurationByUrl(zaakUrl))
        ) { "No plugin configuration was found for zaak with URL $zaakUrl" }

        val initiator = requireNotNull(
            zakenPlugin.getZaakRollen(zaakUrl, RolType.INITIATOR).firstOrNull()
        ) { "No initiator role found for zaak with URL $zaakUrl" }

        return requireNotNull(
            initiator.betrokkeneIdentificatie.let {
                when (it) {
                    is RolNatuurlijkPersoon -> TaakIdentificatie(
                        TaakIdentificatie.TYPE_BSN,
                        requireNotNull(it.inpBsn) {
                            "Zaak initiator did not have valid inpBsn BSN"
                        }
                    )

                    is RolNietNatuurlijkPersoon -> TaakIdentificatie(
                        TaakIdentificatie.TYPE_KVK,
                        requireNotNull(it.annIdentificatie) {
                            "Zaak initiator did not have valid annIdentificatie KVK"
                        }
                    )

                    else -> null
                }
            }
        ) { "Could not map initiator identificatie (value=${initiator.betrokkeneIdentificatie}) for zaak with URL $zaakUrl to TaakIdentificatie" }
    }

    internal fun getTaakForm(
        formType: TaakFormType,
        formTypeId: String?,
        formTypeUrl: String?
    ): TaakForm {
        return TaakForm(
            formType,
            when (formType) {
                TaakFormType.ID -> formTypeId
                    ?: throw IllegalStateException("formTypeId can not be null when formType ID has been chosen")

                TaakFormType.URL -> formTypeUrl
                    ?: throw IllegalStateException("formTypeUrl can not be null when formType URL has been chosen")
            }
        )
    }

    internal fun getTaakData(
        delegateTask: DelegateTask,
        sendData: List<DataBindingConfig>,
        documentId: String
    ): Map<String, Any> {
        val sendDataValuesResolvedMap = valueResolverService.resolveValues(
            delegateTask.processInstanceId,
            delegateTask,
            sendData.map { it.value }
        )

        if (sendData.size != sendDataValuesResolvedMap.size) {
            val failedValues = sendData
                .filter { !sendDataValuesResolvedMap.containsKey(it.value) }
                .joinToString(", ") { "'${it.key}' = '${it.value}'" }
            throw IllegalArgumentException(
                "Error in sendData for task: '${delegateTask.taskDefinitionKey}' and documentId: '${documentId}'. Failed to resolve values: $failedValues".trimMargin()
            )
        }

        val sendDataResolvedMap = sendData.associate { it.key to sendDataValuesResolvedMap[it.value] }
        val jsonPatchBuilder = JsonPatchBuilder()
        val taakData = objectMapper.createObjectNode()

        sendDataResolvedMap.forEach {
            val path = JsonPointer.valueOf(it.key)
            val valueNode = objectMapper.valueToTree<JsonNode>(it.value)
            jsonPatchBuilder.addJsonNodeValue(taakData, path, valueNode)
        }

        JsonPatchService.apply(jsonPatchBuilder.build(), taakData)

        return objectMapper.convertValue(taakData)
    }

    internal fun changeStatus(taakObject: TaakObject, status: TaakStatus): TaakObject {
        return TaakObject(
            taakObject.identificatie,
            taakObject.data,
            taakObject.title,
            status,
            taakObject.formulier,
            taakObject.verwerkerTaakId,
            taakObject.zaakUrl,
            taakObject.verloopdatum,
            taakObject.verzondenData,
        )
    }

    internal fun changeDataInPortalTaakObject(
        portaalTaakMetaObject: ObjectWrapper,
        convertValue: JsonNode
    ): ObjectRequest {
        return ObjectRequest(
            type = portaalTaakMetaObject.type,
            record = ObjectRecord(
                data = convertValue,
                correctedBy = portaalTaakMetaObject.record.correctedBy,
                endAt = portaalTaakMetaObject.record.endAt,
                index = portaalTaakMetaObject.record.index,
                geometry = portaalTaakMetaObject.record.geometry,
                registrationAt = portaalTaakMetaObject.record.registrationAt,
                startAt = portaalTaakMetaObject.record.startAt,
                typeVersion = portaalTaakMetaObject.record.typeVersion
            )
        )
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}
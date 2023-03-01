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

package com.ritense.verzoek

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.document.service.DocumentService
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.notificatiesapi.exception.NotificatiesNotificationEventException
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import com.ritense.verzoek.domain.CopyStrategy
import com.ritense.verzoek.domain.VerzoekProperties
import org.springframework.context.event.EventListener
import java.net.URI

class VerzoekPluginEventListener(
    private val pluginService: PluginService,
    private val objectManagementService: ObjectManagementService,
    private val documentService: DocumentService,
    private val zaaktypeUrlProvider: ZaaktypeUrlProvider,
    private val processDocumentService: ProcessDocumentService
) {

    @EventListener(NotificatiesApiNotificationReceivedEvent::class)
    fun createZaakFromNotificatie(event: NotificatiesApiNotificationReceivedEvent) {
        val objectType = event.kenmerken["objectType"]

        if (!event.kanaal.equals("objecten", ignoreCase = true) ||
            !event.actie.equals("create", ignoreCase = true) ||
            objectType == null
        ) {

            return
        }

        val objectManagement = objectManagementService.findByObjectTypeId(objectType.substringAfterLast("/")) ?: return

        pluginService.createInstance(VerzoekPlugin::class.java) { properties: JsonNode ->
            properties.get("objectManagementId").textValue().equals(objectManagement.id.toString())
        }?.run {
            val verzoekObjectData = getVerzoekObjectData(objectManagement, event)
            val verzoekTypeProperties = getVerzoekTypeProperties(verzoekObjectData)
            val document = createDocument(verzoekTypeProperties, verzoekObjectData)

            val initiatorType = if (verzoekObjectData.has("kvk")) {
                "kvk"
            } else {
                "bsn"
            }
            val zaakTypeUrl = zaaktypeUrlProvider.getZaaktypeUrl(document.definitionId().name())
            val startProcessRequest = StartProcessForDocumentRequest(
                document.id(), processToStart, mapOf(
                    "RSIN" to this.rsin.toString(),
                    "zaakTypeUrl" to zaakTypeUrl.toString(),
                    "rolTypeUrl" to verzoekTypeProperties.initiatorRoltypeUrl.toString(),
                    "rolDescription" to verzoekTypeProperties.initiatorRolDescription,
                    "verzoekObjectUrl" to event.resourceUrl,
                    "initiatorType" to initiatorType,
                    "initiatorValue" to verzoekObjectData.get(initiatorType).textValue(),
                    "processDefinitionKey" to verzoekTypeProperties.processDefinitionKey,
                    "documentUrls" to getDocumentUrls(verzoekObjectData)
                )
            )

            startProcess(startProcessRequest)
        }
    }

    private fun getDocumentUrls(verzoekObjectData: JsonNode): List<String> {
        val documentList = arrayListOf<String>()

        verzoekObjectData.get("pdf_url")?.let {
            documentList.add(it.textValue())
        }
        verzoekObjectData.get("attachments")?.let {
            if (it.isArray) {
                it.toList().forEach { child ->
                    documentList.add(child.textValue())
                }
            }
        }
        return documentList
    }

    private fun getVerzoekObjectData(
        objectManagement: ObjectManagement,
        event: NotificatiesApiNotificationReceivedEvent
    ): JsonNode {
        val objectenApiPlugin =
            pluginService.createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        val verzoekObjectData = objectenApiPlugin.getObject(URI(event.resourceUrl)).record.data
            ?: throw NotificatiesNotificationEventException(
                "Verzoek meta data was empty!"
            )
        return verzoekObjectData
    }

    private fun VerzoekPlugin.getVerzoekTypeProperties(verzoekObjectData: JsonNode): VerzoekProperties {
        val verzoekType = verzoekObjectData.get("type")?.textValue()
        val verzoekTypeProperties = verzoekProperties.firstOrNull { props -> props.type.equals(verzoekType, true) }
            ?: throw NotificatiesNotificationEventException(
                "Could not find properties of type $verzoekType"
            )
        return verzoekTypeProperties
    }

    private fun createDocument(
        verzoekTypeProperties: VerzoekProperties,
        verzoekObjectData: JsonNode
    ): Document {
        return documentService.createDocument(
            NewDocumentRequest(
                verzoekTypeProperties.caseDefinitionName,
                getDocumentContent(verzoekTypeProperties, verzoekObjectData)
            )
        ).also { result ->
            if (result.errors().size > 0) {
                throw NotificatiesNotificationEventException(
                    "Could not create document for case ${verzoekTypeProperties.caseDefinitionName}\n" +
                            "Reason:\n" +
                            result.errors().joinToString(separator = "\n - ")
                )
            }
        }.resultingDocument().orElseThrow()
    }

    private fun getDocumentContent(
        verzoekTypeProperties: VerzoekProperties,
        verzoekObjectData: JsonNode
    ): JsonNode {
        val verzoekDataData = verzoekObjectData.get("data") ?: throw NotificatiesNotificationEventException(
            "Verzoek Object data was empty, for verzoek with type '${verzoekTypeProperties.type}'"
        )

        return if (verzoekTypeProperties.copyStrategy == CopyStrategy.FULL) {
            verzoekDataData
        } else {
            val documentContent = jacksonObjectMapper().createObjectNode()
            val jsonPatchBuilder = JsonPatchBuilder()
            verzoekTypeProperties.mapping?.map {
                val verzoekDataItem = verzoekDataData.at(it.key)
                if (verzoekDataItem.isMissingNode) {
                    throw NotificatiesNotificationEventException(
                        "Missing Verzoek data at path '${it.key}', for Verzoek with type '${verzoekTypeProperties.type}'"
                    )
                }
                jsonPatchBuilder.addJsonNodeValue(documentContent, JsonPointer.valueOf(it.value), verzoekDataItem)
            }
            JsonPatchService.apply(jsonPatchBuilder.build(), documentContent)
            documentContent
        }
    }

    private fun startProcess(startProcessRequest: StartProcessForDocumentRequest) {
        val result = processDocumentService.startProcessForDocument(startProcessRequest)
        if (result == null || result.errors().size > 0) {
            throw NotificatiesNotificationEventException(
                "Could not start process ${startProcessRequest.processDefinitionKey}\n" +
                        "Reason:\n" +
                        result.errors().joinToString(separator = "\n - ")
            )
        }
    }
}

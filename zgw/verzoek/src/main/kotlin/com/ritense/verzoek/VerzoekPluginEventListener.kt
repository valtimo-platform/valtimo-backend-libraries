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

package com.ritense.verzoek

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.document.service.DocumentService
import com.ritense.logging.withLoggingContext
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
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import com.ritense.processdocument.resolver.DocumentJsonValueResolverFactory.Companion.PREFIX as DOC_PREFIX
import com.ritense.valueresolver.ProcessVariableValueResolverFactory.Companion.PREFIX as PV_PREFIX

@Transactional
open class VerzoekPluginEventListener(
    private val pluginService: PluginService,
    private val objectManagementService: ObjectManagementService,
    private val documentService: DocumentService,
    private val zaaktypeUrlProvider: ZaaktypeUrlProvider,
    private val processDocumentService: ProcessDocumentService,
    private val objectMapper: ObjectMapper,
) {

    @Transactional
    @RunWithoutAuthorization
    @EventListener(NotificatiesApiNotificationReceivedEvent::class)
    open fun createZaakFromNotificatie(event: NotificatiesApiNotificationReceivedEvent) {
        val objectType = event.kenmerken["objectType"]

        if (!event.kanaal.equals("objecten", ignoreCase = true) ||
            !event.actie.equals("create", ignoreCase = true) ||
            objectType == null
        ) {
            logger.debug { "Notificaties API event does not match criteria for creating a zaak. Ignoring." }
            return
        }

        val objectManagement = objectManagementService.findByObjectTypeId(objectType.substringAfterLast("/")) ?: return

        pluginService.createInstance(VerzoekPlugin::class.java) { properties: JsonNode ->
            properties.get("verzoekProperties")
                .any { it.get("objectManagementId").textValue().equals(objectManagement.id.toString()) }
        }?.run {
            val verzoekObjectData = getVerzoekObjectData(objectManagement, event)
            val verzoekTypeProperties = getVerzoekTypeProperties(verzoekObjectData, event) ?: return
            val document = createDocument(verzoekTypeProperties, verzoekObjectData)
            withLoggingContext(JsonSchemaDocument::class, document.id()) {
                val zaakTypeUrl = zaaktypeUrlProvider.getZaaktypeUrl(document.definitionId().name())
                val initiatorType = if (verzoekObjectData.has("kvk")) {
                    "kvk"
                } else {
                    "bsn"
                }

                val verzoekVariables = mutableMapOf(
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

                addVerzoekVariablesToProcessVariable(verzoekTypeProperties, verzoekObjectData, verzoekVariables)

                val startProcessRequest = StartProcessForDocumentRequest(
                    document.id(), processToStart, verzoekVariables
                )

                return@withLoggingContext startProcess(startProcessRequest)
            }
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
        logger.debug { "Fetching verzoek object data from URL '${event.resourceUrl}'" }
        val objectenApiPlugin =
            pluginService.createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        val verzoekObjectData = objectenApiPlugin.getObject(URI(event.resourceUrl)).record.data
            ?: throw NotificatiesNotificationEventException(
                "Verzoek meta data was empty!"
            )
        logger.debug { "Fetched verzoek object data from URL '${event.resourceUrl}' successfully" }
        return verzoekObjectData
    }

    private fun VerzoekPlugin.getVerzoekTypeProperties(
        verzoekObjectData: JsonNode,
        event: NotificatiesApiNotificationReceivedEvent
    ): VerzoekProperties? {
        val verzoekType = verzoekObjectData.get("type")?.textValue()
        val verzoekTypeProperties = verzoekProperties.firstOrNull { props -> props.type.equals(verzoekType, true) }
        if (verzoekTypeProperties == null && verzoekType != null) {
            throw NotificatiesNotificationEventException(
                "Failed to find verzoek configuration of type $verzoekType. For object ${event.resourceUrl}"
            )
        }
        logger.debug { "Found verzoek type properties for type '$verzoekType' for object at URL '${event.resourceUrl}'" }
        return verzoekTypeProperties
    }

    private fun createDocument(
        verzoekTypeProperties: VerzoekProperties,
        verzoekObjectData: JsonNode
    ): Document {
        logger.debug { "Creating document for verzoek of type '${verzoekTypeProperties.type}'" }
        return AuthorizationContext.runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    verzoekTypeProperties.caseDefinitionName,
                    getDocumentContent(verzoekTypeProperties, verzoekObjectData)
                )
            )
        }.also { result ->
            if (result.errors().size > 0) {
                throw NotificatiesNotificationEventException(
                    "Could not create document for case ${verzoekTypeProperties.caseDefinitionName}\n" +
                        "Reason:\n" +
                        result.errors().joinToString(separator = "\n - ")
                )
            }
        }.resultingDocument().orElseThrow().also { document ->
            logger.info { "Document with id '${document.id().id}' created successfully for verzoek of type '${verzoekTypeProperties.type}'" }
        }
    }

    private fun getDocumentContent(
        verzoekTypeProperties: VerzoekProperties,
        verzoekObjectData: JsonNode
    ): JsonNode {
        val verzoekDataData = verzoekObjectData.get("data") ?: throw NotificatiesNotificationEventException(
            "Verzoek Object data was empty, for verzoek with type '${verzoekTypeProperties.type}'"
        )

        logger.debug { "Building document content for verzoek type '${verzoekTypeProperties.type}'" }

        return if (verzoekTypeProperties.copyStrategy == CopyStrategy.FULL) {
            verzoekDataData
        } else {
            val documentContent = objectMapper.createObjectNode()
            val jsonPatchBuilder = JsonPatchBuilder()
            verzoekTypeProperties.mapping?.map {
                val verzoekDataItem = verzoekDataData.at(it.source)
                if (!verzoekDataItem.isMissingNode) {
                    if (it.target.startsWith(DOC_PREFIX)) {
                        val documentPath = JsonPointer.valueOf(it.target.substringAfter(delimiter = ":"))
                        jsonPatchBuilder.addJsonNodeValue(documentContent, documentPath, verzoekDataItem)
                    }
                } else {
                    logger.debug { "Missing Verzoek data of Verzoek type '${verzoekTypeProperties.type}' at path '${it.source}' is not mapped!" }
                }
            }
            JsonPatchService.apply(jsonPatchBuilder.build(), documentContent)
            logger.debug { "Document content for verzoek of type '${verzoekTypeProperties.type}' created successfully" }
            return documentContent
        }
    }

    private fun startProcess(startProcessRequest: StartProcessForDocumentRequest) {
        logger.debug { "Starting process '${startProcessRequest.processDefinitionKey}' for document with id '${startProcessRequest.documentId.id}'" }
        val result = processDocumentService.startProcessForDocument(startProcessRequest)
        if (result == null || result.errors().size > 0) {
            throw NotificatiesNotificationEventException(
                "Could not start process ${startProcessRequest.processDefinitionKey}\n" +
                    "Reason:\n" +
                    result.errors().joinToString(separator = "\n - ")
            )
        }
        logger.info {
            "Process of type '${startProcessRequest.processDefinitionKey}' with id '${
                result.processInstanceId().get()
            }' for document with id '${startProcessRequest.documentId.id}' started successfully."
        }
    }

    private fun addVerzoekVariablesToProcessVariable(
        verzoekTypeProperties: VerzoekProperties,
        verzoekObjectData: JsonNode,
        verzoekVariables: MutableMap<String, Any?>
    ) {
        val verzoekData = verzoekObjectData.get("data") ?: throw NotificatiesNotificationEventException(
            "Verzoek Object data was empty, for verzoek with type '${verzoekTypeProperties.type}'"
        )

        if (verzoekTypeProperties.copyStrategy == CopyStrategy.SPECIFIED) {
            logger.debug { "Adding specified verzoek variables to process for verzoek of type '${verzoekTypeProperties.type}'" }
            verzoekTypeProperties.mapping?.map {
                if (it.target.startsWith(PV_PREFIX)) {
                    val verzoekDataItem = verzoekData.at(it.source)
                    val key = it.target.substringAfter(delimiter = "/")

                    if (verzoekDataItem.isMissingNode || verzoekDataItem.isNull) {
                        verzoekVariables[key] = null
                        logger.debug { "Missing Verzoek data of Verzoek type '${verzoekTypeProperties.type}' at path '${it.source}' is not mapped!" }
                    } else if (verzoekDataItem.isValueNode || verzoekDataItem.isArray || verzoekDataItem.isObject) {
                        verzoekVariables[key] = objectMapper.treeToValue(verzoekDataItem, Object::class.java)
                    } else {
                        verzoekVariables[key] = verzoekDataItem.asText()
                    }
                }
            }
            logger.debug { "Verzoek variables added to process successfully for verzoek of type '${verzoekTypeProperties.type}'" }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

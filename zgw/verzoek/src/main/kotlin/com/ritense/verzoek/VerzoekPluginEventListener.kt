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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
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
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import com.ritense.verzoek.domain.CopyStrategy
import com.ritense.verzoek.domain.VerzoekProperties
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import com.ritense.processdocument.resolver.DocumentJsonValueResolverFactory.Companion.PREFIX as DOC_PREFIX
import com.ritense.valueresolver.ProcessVariableValueResolverFactory.Companion.PREFIX as PV_PREFIX

@SkipComponentScan
@Component
@Transactional
class VerzoekPluginEventListener(
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
    fun createZaakFromNotificatie(event: NotificatiesApiNotificationReceivedEvent) {
        val objectType = event.kenmerken["objectType"]
        if (objectType == null) {
            logger.debug { "VerzoekPlugin is ignoring Notificaties API event: Event 'objectType' is null" }
            return
        }
        if (!event.kanaal.equals("objecten", ignoreCase = true)) {
            logger.debug { "VerzoekPlugin is ignoring Notificaties API event: Event kanaal '${event.kanaal}' doesn't match 'objecten'" }
            return
        }
        if (!event.actie.equals("create", ignoreCase = true)) {
            logger.debug { "VerzoekPlugin is ignoring Notificaties API event: Event actie '${event.actie}' doesn't match 'create'" }
            return
        }
        val objectManagement = objectManagementService.findByObjectTypeId(objectType.substringAfterLast("/"))
        if (objectManagement == null) {
            logger.debug { "VerzoekPlugin is ignoring Notificaties API event: No object management found for objectType '$objectType'" }
            return
        }
        val verzoekPlugin = pluginService.createInstance(VerzoekPlugin::class.java) { properties ->
            properties["verzoekProperties"]
                .any { it["objectManagementId"].textValue() == objectManagement.id.toString() }
        }
        if (verzoekPlugin == null) {
            logger.debug { "VerzoekPlugin is ignoring Notificaties API event: No VerzoekPlugin found that uses ObjectManagement: '${objectManagement.title}'" }
            return
        }

        verzoekPlugin.run {
            val verzoekObjectWrapper = getVerzoekObject(objectManagement, event)
            val verzoekObject = objectMapper.valueToTree<ObjectNode>(verzoekObjectWrapper)
            val verzoekObjectData = verzoekObjectWrapper.record.data as ObjectNode?
                ?: throw NotificatiesNotificationEventException("VerzoekObject /record/data cannot be found!")
            val verzoekTypeProperties = getVerzoekTypeProperties(verzoekObjectData, event)
            if (verzoekTypeProperties == null) {
                val verzoekType = verzoekObjectData["type"]?.textValue()
                logger.debug { "VerzoekPlugin is ignoring Notificaties API event: No verzoek plugin found for type '${verzoekType}'" }
                return
            }

            logger.info { "Received verzoek notification. Verzoek objectUrl: ${event.resourceUrl}" }
            val document = createDocument(verzoekTypeProperties, verzoekObject)
            withLoggingContext(JsonSchemaDocument::class, document.id()) {
                val zaakTypeUrl = zaaktypeUrlProvider.getZaaktypeUrl(document.definitionId().name())
                val initiatorType = if (verzoekObjectData.has("kvk")) {
                    "kvk"
                } else if (verzoekObjectData.has("bsn")) {
                    "bsn"
                } else {
                    null
                }

                val verzoekVariables = objectMapper.treeToValue<MutableMap<String, Any?>>(verzoekObjectData)
                verzoekVariables.remove("data")
                verzoekVariables += mutableMapOf(
                    "RSIN" to rsin.toString(),
                    "zaakTypeUrl" to zaakTypeUrl.toString(),
                    "rolTypeUrl" to verzoekTypeProperties.initiatorRoltypeUrl.toString(),
                    "rolDescription" to verzoekTypeProperties.initiatorRolDescription,
                    "verzoekObjectUrl" to event.resourceUrl,
                    "initiatorType" to initiatorType,
                    "processDefinitionKey" to verzoekTypeProperties.processDefinitionKey,
                    "documentUrls" to getDocumentUrls(verzoekObjectData)
                )
                initiatorType?.let { verzoekVariables["initiatorValue"] = verzoekObjectData[it].textValue() }

                addVerzoekVariablesToProcessVariable(verzoekTypeProperties, verzoekObject, verzoekVariables)

                val startProcessRequest = StartProcessForDocumentRequest(
                    document.id(), processToStart, verzoekVariables
                )

                return@withLoggingContext startProcess(startProcessRequest)
            }
        }
    }

    protected fun getVerzoekDataFromPath(verzoekObject: JsonNode, path: String): JsonNode {
        return if (path.startsWith("object:")) {
            verzoekObject.at(path.substringAfterLast("object:"))
        } else {
            val verzoekDataData = verzoekObject["record"]["data"]["data"] ?: throw NotificatiesNotificationEventException(
                "VerzoekObject /record/data/data cannot be found! For verzoek with type '${verzoekObject["type"]}'"
            )
            verzoekDataData.at(path)
        }
    }

    private fun getDocumentUrls(verzoekObjectData: JsonNode): List<String> {
        val documentList = arrayListOf<String>()

        verzoekObjectData["pdf_url"]?.let {
            documentList.add(it.textValue())
        }
        verzoekObjectData["attachments"]?.let {
            if (it.isArray) {
                it.toList().forEach { child ->
                    documentList.add(child.textValue())
                }
            }
        }
        return documentList
    }

    private fun getVerzoekObject(
        objectManagement: ObjectManagement,
        event: NotificatiesApiNotificationReceivedEvent
    ): ObjectWrapper {
        logger.debug { "Fetching verzoek object data from URL '${event.resourceUrl}'" }
        val objectenApiPlugin =
            pluginService.createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        val verzoekObjectData = objectenApiPlugin.getObject(URI(event.resourceUrl))

        logger.debug { "Fetched verzoek object data from URL '${event.resourceUrl}' successfully" }
        return verzoekObjectData
    }

    private fun VerzoekPlugin.getVerzoekTypeProperties(
        verzoekObjectData: JsonNode,
        event: NotificatiesApiNotificationReceivedEvent
    ): VerzoekProperties? {
        val verzoekType = verzoekObjectData["type"]?.textValue()
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
        verzoekObject: ObjectNode
    ): Document {
        logger.debug { "Creating document for verzoek of type '${verzoekTypeProperties.type}'" }
        return AuthorizationContext.runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    verzoekTypeProperties.caseDefinitionName,
                    getDocumentContent(verzoekTypeProperties, verzoekObject)
                )
            )
        }.also { result ->
            if (result.errors().isNotEmpty()) {
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
        verzoekObject: ObjectNode
    ): JsonNode {
        val verzoekDataData = verzoekObject["record"]["data"]["data"] ?: throw NotificatiesNotificationEventException(
            "VerzoekObject /record/data/data cannot be found! For verzoek with type '${verzoekTypeProperties.type}'"
        )

        logger.debug { "Building document content for verzoek type '${verzoekTypeProperties.type}'" }

        return if (verzoekTypeProperties.copyStrategy == CopyStrategy.FULL) {
            verzoekDataData
        } else {
            val documentContent = objectMapper.createObjectNode()
            val jsonPatchBuilder = JsonPatchBuilder()
            verzoekTypeProperties.mapping
                ?.filter { it.target.startsWith(DOC_PREFIX) }
                ?.map {
                    val verzoekDataItem = getVerzoekDataFromPath(verzoekObject, it.source)
                    if (!verzoekDataItem.isMissingNode) {
                        val documentPath = JsonPointer.valueOf(it.target.substringAfter(delimiter = ":"))
                        jsonPatchBuilder.addJsonNodeValue(documentContent, documentPath, verzoekDataItem)
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
        if (result == null || result.errors().isNotEmpty()) {
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
        verzoekObject: JsonNode,
        verzoekVariables: MutableMap<String, Any?>
    ) {
        if (verzoekTypeProperties.copyStrategy == CopyStrategy.SPECIFIED) {
            logger.debug { "Adding specified verzoek variables to process for verzoek of type '${verzoekTypeProperties.type}'" }
            verzoekTypeProperties.mapping
                ?.filter { it.target.startsWith(PV_PREFIX) }
                ?.map {
                    val verzoekDataItem = getVerzoekDataFromPath(verzoekObject, it.source)
                    val key = it.target.substringAfter(delimiter = ":").substringAfter(delimiter = "/")

                    if (verzoekDataItem.isMissingNode || verzoekDataItem.isNull) {
                        verzoekVariables[key] = null
                        logger.debug { "Missing Verzoek data of Verzoek type '${verzoekTypeProperties.type}' at path '${it.source}' is not mapped!" }
                    } else if (verzoekDataItem.isValueNode || verzoekDataItem.isArray || verzoekDataItem.isObject) {
                        verzoekVariables[key] = objectMapper.treeToValue(verzoekDataItem, Object::class.java)
                    } else {
                        verzoekVariables[key] = verzoekDataItem.asText()
                    }
                }
            logger.debug { "Verzoek variables added to process successfully for verzoek of type '${verzoekTypeProperties.type}'" }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

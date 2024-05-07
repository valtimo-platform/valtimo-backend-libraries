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

package com.ritense.documentenapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.ritense.documentenapi.DocumentenApiPlugin.Companion.PLUGIN_KEY
import com.ritense.documentenapi.client.CreateDocumentRequest
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.client.DocumentStatusType
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.client.PatchDocumentRequest
import com.ritense.documentenapi.event.DocumentCreated
import com.ritense.documentenapi.service.DocumentDeleteHandler
import com.ritense.documentenapi.service.DocumentenApiVersionService
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginEvent
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.EventType
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.contract.validation.Url
import com.ritense.zgw.domain.Vertrouwelijkheid
import jakarta.validation.ValidationException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.hibernate.validator.constraints.Length
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.io.InputStream
import java.net.URI
import java.time.LocalDate

@Plugin(
    key = PLUGIN_KEY,
    title = "Documenten API",
    description = "Connects to the Documenten API to store documents"
)
class DocumentenApiPlugin(
    private val client: DocumentenApiClient,
    private val storageService: TemporaryResourceStorageService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
    private val documentDeleteHandlers: List<DocumentDeleteHandler>,
    private val documentenApiVersionService: DocumentenApiVersionService,
) {
    @Url
    @PluginProperty(key = URL_PROPERTY, secret = false)
    lateinit var url: URI

    @Length(min = 9, max = 9)
    @PluginProperty(key = "bronorganisatie", secret = false)
    lateinit var bronorganisatie: String

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: DocumentenApiAuthentication

    @PluginProperty(key = "apiVersion", secret = false, required = false)
    var apiVersion: String? = null

    @PluginAction(
        key = "store-temp-document",
        title = "Store temporary document",
        description = "Store a temporary document in the Documenten API",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun storeTemporaryDocument(
        execution: DelegateExecution,
        @PluginActionProperty fileName: String?,
        @PluginActionProperty confidentialityLevel: String?,
        @PluginActionProperty title: String?,
        @PluginActionProperty description: String?,
        @PluginActionProperty localDocumentLocation: String,
        @PluginActionProperty storedDocumentUrl: String,
        @PluginActionProperty informatieobjecttype: String,
        @PluginActionProperty taal: String = DEFAULT_LANGUAGE,
        @PluginActionProperty status: DocumentStatusType = DocumentStatusType.DEFINITIEF
    ) {
        val documentLocation = execution.getVariable(localDocumentLocation) as String?
            ?: throw IllegalStateException("Failed to store document. No process variable '$localDocumentLocation' found.")
        val contentAsInputStream = storageService.getResourceContentAsInputStream(documentLocation)
        val metadata = storageService.getResourceMetadata(documentLocation)
        val fileNameNotNull = fileName ?: metadata[MetadataType.FILE_NAME.key] as String

        storeDocument(
            execution = execution,
            metadata = metadata,
            title = title ?: fileNameNotNull,
            confidentialityLevel = confidentialityLevel,
            status = status,
            language = taal,
            filename = fileNameNotNull,
            contentAsInputStream = contentAsInputStream,
            description = description,
            informationObjectType = informatieobjecttype,
            storedDocumentUrl = storedDocumentUrl
        )
    }

    @PluginAction(
        key = "store-uploaded-document",
        title = "Store uploaded document",
        description = "Store an uploaded document in the Documenten API",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun storeUploadedDocument(
        execution: DelegateExecution
    ) {
        val resourceId = execution.getVariable(RESOURCE_ID_PROCESS_VAR) as String?
            ?: throw IllegalStateException("Failed to store document. No process variable '$RESOURCE_ID_PROCESS_VAR' found.")
        val contentAsInputStream = storageService.getResourceContentAsInputStream(resourceId)
        val metadata = storageService.getResourceMetadata(resourceId)

        storeDocument(
            execution = execution,
            metadata = metadata,
            title = metadata["title"] as String,
            confidentialityLevel = metadata["confidentialityLevel"] as String?,
            status = getStatusFromMetaData(metadata),
            language = metadata["language"] as String?,
            filename = getFilenameFromMetaData(metadata),
            contentAsInputStream = contentAsInputStream,
            description = metadata["description"] as String?,
            informationObjectType = metadata["informatieobjecttype"] as String,
            storedDocumentUrl = DOCUMENT_URL_PROCESS_VAR,
        )
    }

    @PluginAction(
        key = "download-document",
        title = "Download document",
        description = "Download a document from the Documenten API and store it as a temporary document",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun downloadInformatieObject(
        execution: DelegateExecution,
        @PluginActionProperty processVariableName: String? = null
    ): String {
        val documentUrlString = execution.getVariable(DOCUMENT_URL_PROCESS_VAR) as String?
            ?: throw IllegalStateException("Failed to download document. No process variable '$DOCUMENT_URL_PROCESS_VAR' found.")
        if (!documentUrlString.startsWith(url.toASCIIString())) {
            throw IllegalStateException("Failed to download document with url '$documentUrlString'. Document isn't part of Documenten API with url '$url'.")
        }
        val documentUrl = URI(documentUrlString)
        val metaData = client.getInformatieObject(authenticationPluginConfiguration, documentUrl)
        val content = client.downloadInformatieObjectContent(authenticationPluginConfiguration, documentUrl)

        val metaDataMap = objectMapper.convertValue<MutableMap<String, Any>>(metaData)
        metaDataMap[MetadataType.DOCUMENT_ID.key] = execution.businessKey
        metaDataMap["title"] = metaData.titel
        metaData.beschrijving?.let { metaDataMap["description"] = it }
        metaData.bestandsnaam?.let { metaDataMap[MetadataType.FILE_NAME.key] = it }

        val tempResourceId = storageService.store(
            inputStream = content,
            metadata = metaDataMap
        )

        execution.setVariable(processVariableName ?: RESOURCE_ID_PROCESS_VAR, tempResourceId)

        return tempResourceId
    }

    fun downloadInformatieObject(objectId: String): InputStream {
        return client.downloadInformatieObjectContent(authenticationPluginConfiguration, url, objectId)
    }

    fun getInformatieObject(objectId: String): DocumentInformatieObject {
        return client.getInformatieObject(authenticationPluginConfiguration, url, objectId)
    }

    fun getInformatieObject(objectUrl: URI): DocumentInformatieObject {
        return client.getInformatieObject(authenticationPluginConfiguration, objectUrl)
    }

    fun getInformatieObjecten(documentSearchRequest: DocumentSearchRequest, pageable: Pageable): Page<DocumentInformatieObject> {
        return client.getInformatieObjecten(authenticationPluginConfiguration, url, pageable, documentSearchRequest)
    }

    fun deleteInformatieObject(objectUrl: URI) {
        documentDeleteHandlers.forEach { it.preDocumentDelete(objectUrl) }
        client.deleteInformatieObject(authenticationPluginConfiguration, objectUrl)
    }

    fun createInformatieObjectUrl(objectId: String): URI {
        return UriComponentsBuilder
            .fromUri(url)
            .pathSegment("enkelvoudiginformatieobjecten", objectId)
            .build()
            .toUri()
    }

    fun modifyInformatieObject(documentUrl: URI, patchDocumentRequest: PatchDocumentRequest): DocumentInformatieObject {
        val documentLock = client.lockInformatieObject(authenticationPluginConfiguration, documentUrl)
        try {
            patchDocumentRequest.lock = documentLock.lock
            val modifiedDocument =
                client.modifyInformatieObject(authenticationPluginConfiguration, documentUrl, patchDocumentRequest)
            return modifiedDocument
        } finally {
            client.unlockInformatieObject(authenticationPluginConfiguration, documentUrl, documentLock)
        }
    }

    @PluginEvent(invokedOn = [EventType.CREATE, EventType.UPDATE])
    fun onSave() {
        if (apiVersion != null && !documentenApiVersionService.isValidVersion(apiVersion!!)) {
            throw ValidationException("Unknown API version '$apiVersion'.")
        }
    }

    private fun storeDocument(
        execution: DelegateExecution,
        metadata: Map<String, Any>,
        title: String,
        confidentialityLevel: String?,
        status: DocumentStatusType,
        language: String?,
        filename: String?,
        contentAsInputStream: InputStream,
        description: String?,
        informationObjectType: String,
        storedDocumentUrl: String,
    ) {
        val request = CreateDocumentRequest(
            bronorganisatie = bronorganisatie,
            creatiedatum = getLocalDateFromMetaData(metadata, "creationDate", LocalDate.now())!!,
            titel = title,
            vertrouwelijkheidaanduiding = Vertrouwelijkheid.fromKey(confidentialityLevel),
            auteur = metadata["author"] as String? ?: DEFAULT_AUTHOR,
            status = status,
            taal = language ?: DEFAULT_LANGUAGE,
            bestandsnaam = filename,
            bestandsomvang = (metadata[MetadataType.FILE_SIZE.key] as String?)?.toLong(),
            inhoud = contentAsInputStream,
            beschrijving = description,
            ontvangstdatum = getLocalDateFromMetaData(metadata, "receiptDate"),
            verzenddatum = getLocalDateFromMetaData(metadata, "sendDate"),
            informatieobjecttype = informationObjectType,
            formaat = metadata["contentType"] as String?,
        )

        val documentCreateResult = client.storeDocument(authenticationPluginConfiguration, url, request)

        val event = DocumentCreated(
            documentCreateResult.url,
            documentCreateResult.auteur,
            documentCreateResult.bestandsnaam,
            documentCreateResult.bestandsomvang,
            documentCreateResult.beginRegistratie
        )
        applicationEventPublisher.publishEvent(event)
        execution.setVariable(storedDocumentUrl, documentCreateResult.url)
    }

    private fun getLocalDateFromMetaData(
        metadata: Map<String, Any>,
        field: String,
        default: LocalDate? = null
    ): LocalDate? {
        val localDateString = metadata[field] as String?
        return if (localDateString != null) {
            LocalDate.parse(localDateString)
        } else {
            default
        }
    }

    private fun getStatusFromMetaData(metadata: Map<String, Any>): DocumentStatusType {
        val status = metadata["status"] as String?
        return if (status != null) {
            DocumentStatusType.fromKey(status) ?:
               throw IllegalStateException("Failed to store document. Invalid status '$status' found in metadata.")
        } else {
            DocumentStatusType.DEFINITIEF
        }
    }

    private fun getFilenameFromMetaData(metadata: Map<String, Any>): String? {
        return metadata["filename"] as String? ?: metadata[MetadataType.FILE_NAME.name] as String?
    }

    companion object {
        const val PLUGIN_KEY = "documentenapi"
        const val URL_PROPERTY = "url"
        const val DEFAULT_AUTHOR = "GZAC"
        const val DEFAULT_LANGUAGE = "nld"
        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val DOCUMENT_URL_PROCESS_VAR = "documentUrl"

        fun findConfigurationByUrl(url: URI) = { properties: JsonNode ->
            url.toString().startsWith(properties.get(URL_PROPERTY).textValue())
        }
    }
}

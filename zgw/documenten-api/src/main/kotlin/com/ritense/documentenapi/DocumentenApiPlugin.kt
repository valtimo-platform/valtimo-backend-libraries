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
import com.ritense.documentenapi.client.BestandsdelenRequest
import com.ritense.documentenapi.client.CreateDocumentRequest
import com.ritense.documentenapi.client.CreateDocumentResult
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.client.DocumentLock
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
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.contract.validation.Url
import com.ritense.zgw.domain.Vertrouwelijkheid
import jakarta.validation.ValidationException
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.hibernate.validator.constraints.Length
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.io.InputStream
import java.net.URI
import java.time.LocalDate
import java.util.UUID

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
    private val pluginService: PluginService
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

        storeDocument(
            execution = execution,
            metadata = metadata,
            titel = title,
            vertrouwelijkheidaanduiding = confidentialityLevel,
            status = status,
            taal = taal,
            bestandsnaam = fileName,
            inhoudAsInputStream = contentAsInputStream,
            beschrijving = description,
            informatieobjecttype = informatieobjecttype,
            storedDocumentKey = storedDocumentUrl
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
            titel = null,
            vertrouwelijkheidaanduiding = null,
            status = null,
            taal = null,
            bestandsnaam = null,
            inhoudAsInputStream = contentAsInputStream,
            beschrijving = null,
            informatieobjecttype = null,
            storedDocumentKey = DOCUMENT_URL_PROCESS_VAR,
        )

    }

    @PluginAction(
        key = "store-uploaded-document-in-parts",
        title = "Store uploaded document in parts",
        description = "Store an uploaded document in the Documenten API in parts using the bestandsdelen api",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun storeUploadedDocumentInParts(
        execution: DelegateExecution
    ) {
        val resourceId = execution.getVariable(RESOURCE_ID_PROCESS_VAR) as String?
            ?: throw IllegalStateException("Failed to store document. No process variable '$RESOURCE_ID_PROCESS_VAR' found.")
        val contentAsInputStream = storageService.getResourceContentAsInputStream(resourceId)
        val metadata = storageService.getResourceMetadata(resourceId)

        storeDocumentInParts(
            execution = execution,
            metadata = metadata,
            bestandsnaam = metadata["filename"].toString(),
            inhoudAsInputStream = contentAsInputStream,
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
        check(documentUrlString.startsWith(url.toASCIIString())) { "Failed to download document with url '$documentUrlString'. Document isn't part of Documenten API with url '$url'." }
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

    fun getInformatieObjecten(
        documentSearchRequest: DocumentSearchRequest,
        pageable: Pageable
    ): Page<DocumentInformatieObject> {
        return client.getInformatieObjecten(authenticationPluginConfiguration, url, pageable, documentSearchRequest)
    }

    fun deleteInformatieObject(objectUrl: URI) {
        logger.info { "Deleting informatie object from documenten API with url $objectUrl" }
        documentDeleteHandlers.forEach { it.preDocumentDelete(objectUrl) }
        client.deleteInformatieObject(authenticationPluginConfiguration, objectUrl)
    }

    fun createInformatieObjectUrl(objectId: String) = UriComponentsBuilder
        .fromUri(url)
        .pathSegment("enkelvoudiginformatieobjecten", objectId)
        .build()
        .toUri()

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
        logger.info { "Documenten API plugin saved" }
        if (apiVersion != null && !documentenApiVersionService.isValidVersion(apiVersion!!)) {
            throw ValidationException("Unknown API version '$apiVersion'.")
        }
    }

    private fun storeDocument(
        execution: DelegateExecution,
        metadata: Map<String, Any?>,
        titel: String?,
        vertrouwelijkheidaanduiding: String?,
        status: DocumentStatusType?,
        taal: String?,
        bestandsnaam: String?,
        inhoudAsInputStream: InputStream,
        beschrijving: String?,
        informatieobjecttype: String?,
        storedDocumentKey: String,
    ): CreateDocumentResult {
        val vertrouwelijkheidaanduidingEnum = Vertrouwelijkheid.fromKey(
            vertrouwelijkheidaanduiding ?: getUploadField(
                metadata,
                VERTROUWELIJKHEIDAANDUIDING_FIELD
            )
        )
        val trefwoorden = (getUploadField(metadata, TREFWOORDEN_FIELD) as String?)
            ?.split(',')
            ?.filter { it.isNotBlank() }

        val request = CreateDocumentRequest(
            bronorganisatie = bronorganisatie,
            creatiedatum = getLocalDateFromMetaData(metadata, CREATIEDATUM_FIELD) ?: LocalDate.now(),
            titel = titel ?: getUploadField(metadata, TITEL_FIELD)!!,
            vertrouwelijkheidaanduiding = vertrouwelijkheidaanduidingEnum,
            auteur = getUploadField(metadata, AUTEUR_FIELD) ?: DEFAULT_AUTHOR,
            status = status ?: getStatusFromMetaData(metadata),
            taal = taal ?: getUploadField(metadata, TAAL_FIELD) ?: DEFAULT_LANGUAGE,
            bestandsnaam = bestandsnaam ?: getUploadField(metadata, BESTANDSNAAM_FIELD),
            bestandsomvang = (metadata[MetadataType.FILE_SIZE.key] as String?)?.toLong(),
            inhoud = inhoudAsInputStream,
            beschrijving = beschrijving ?: getUploadField(metadata, BESCHRIJVING_FIELD),
            ontvangstdatum = getLocalDateFromMetaData(metadata, ONTVANGSTDATUM_FIELD),
            verzenddatum = getLocalDateFromMetaData(metadata, VERZENDDATUM_FIELD),
            informatieobjecttype = informatieobjecttype ?: getUploadField(metadata, INFORMATIEOBJECTTYPE_FIELD),
            formaat = getUploadField(metadata, FORMAAT_FIELD),
            trefwoorden = trefwoorden,
        )
        logger.info { "Store document $request" }
        val documentCreateResult = client.storeDocument(authenticationPluginConfiguration, url, request)

        val event = DocumentCreated(
            documentCreateResult.url,
            documentCreateResult.auteur,
            documentCreateResult.bestandsnaam,
            documentCreateResult.bestandsomvang,
            documentCreateResult.beginRegistratie
        )
        applicationEventPublisher.publishEvent(event)
        execution.setVariable(storedDocumentKey, documentCreateResult.url)
        val documentId = documentCreateResult.url.substringAfterLast('/')
        execution.setVariable(DOCUMENT_ID_PROCESS_VAR, documentId)
        try {
            val test = URI.create(documentCreateResult.url)
            val pluginConfiguration = getDocumentenApiPluginByInformatieobjectUrl(test)
            execution.setVariable(DOWNLOAD_URL_PROCESS_VAR, createDownloadUrl(pluginConfiguration.id.id, documentId))
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to set the $DOWNLOAD_URL_PROCESS_VAR variable in the DelegateExecution", e
            )
        }

        return documentCreateResult
    }

    /**
     * Using the bestandsdelen api a document can be uploaded in chunks. This upload method entails several api calls
     * to store a document:
     *  - First the document metadata is uploaded without the 'inhoud' parameter. The response of this method will
     *    contain a 'lock' parameter that must be used in the next call
     *  - Using the provided lock the contents of the file is uploaded to the bestandsdelen api
     *  - When the complete file is uploaded the unlock api must be called. This will unlock the document enabling it
     *    for download.
     */
    private fun storeDocumentInParts(
        execution: DelegateExecution,
        metadata: Map<String, Any>,
        bestandsnaam: String,
        inhoudAsInputStream: InputStream,
    ) {
        val documentCreateResult = storeDocument(
            execution = execution,
            metadata = metadata,
            titel = null,
            vertrouwelijkheidaanduiding = null,
            status = null,
            taal = null,
            bestandsnaam = bestandsnaam,
            inhoudAsInputStream = InputStream.nullInputStream(),
            beschrijving = null,
            informatieobjecttype = null,
            storedDocumentKey = DOCUMENT_URL_PROCESS_VAR
        )

        val bestandsdelenRequest = BestandsdelenRequest(
            inhoud = inhoudAsInputStream,
            lock = documentCreateResult.getLockFromBestandsdelen()
        )

        client.storeDocumentInParts(
            authenticationPluginConfiguration,
            url,
            bestandsdelenRequest,
            documentCreateResult,
            bestandsnaam
        )

        val documentLock = DocumentLock(documentCreateResult.getLockFromBestandsdelen())
        client.unlockInformatieObject(
            authenticationPluginConfiguration,
            URI.create(documentCreateResult.url),
            documentLock
        )

    }

    private fun getDocumentenApiPluginByInformatieobjectUrl(informatieobjectUrl: URI): PluginConfiguration {
        return checkNotNull(
            pluginService.findPluginConfiguration(
                DocumentenApiPlugin::class.java,
                findConfigurationByUrl(informatieobjectUrl)
            )
        ) { "Could not find ${DocumentenApiPlugin::class.simpleName} configuration for informatieobjectUrl: $informatieobjectUrl" }
    }

    private fun createDownloadUrl(pluginId: UUID, documentId: String): String {
        return "/api/v1/documenten-api/${pluginId}/files/${documentId}/download"
    }

    private fun <T> getUploadField(metadata: Map<String, Any?>, field: List<String>): T? =
        field.firstNotNullOfOrNull { metadata[it] as T? }

    private fun getLocalDateFromMetaData(metadata: Map<String, Any?>, field: List<String>): LocalDate? {
        return getUploadField<String?>(metadata, field)?.let { LocalDate.parse(it) }
    }

    private fun getStatusFromMetaData(metadata: Map<String, Any?>): DocumentStatusType? {
        val status: String? = getUploadField(metadata, STATUS_FIELD)
        return if (status != null) {
            DocumentStatusType.fromKey(status)
                ?: throw IllegalStateException("Failed to store document. Invalid status '$status' found in metadata.")
        } else {
            null
        }
    }

    companion object {
        const val PLUGIN_KEY = "documentenapi"
        const val URL_PROPERTY = "url"
        const val DEFAULT_AUTHOR = "GZAC"
        const val DEFAULT_LANGUAGE = "nld"
        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val DOCUMENT_URL_PROCESS_VAR = "documentUrl"
        const val DOCUMENT_ID_PROCESS_VAR = "documentId"
        const val DOWNLOAD_URL_PROCESS_VAR = "downloadUrl"

        val BESTANDSNAAM_FIELD = listOf("bestandsnaam", "filename", MetadataType.FILE_NAME.key)
        val TITEL_FIELD = listOf("title", "titel") + BESTANDSNAAM_FIELD
        val AUTEUR_FIELD = listOf("author", "auteur", MetadataType.USER.key)
        val BESCHRIJVING_FIELD = listOf("description", "beschrijving")
        val TAAL_FIELD = listOf("language", "taal")
        val INFORMATIEOBJECTTYPE_FIELD = listOf("informatieobjecttype")
        val STATUS_FIELD = listOf("status")
        val VERTROUWELIJKHEIDAANDUIDING_FIELD = listOf("confidentialityLevel", "vertrouwelijkheidaanduiding")
        val ONTVANGSTDATUM_FIELD = listOf("receiptDate", "ontvangstdatum")
        val VERZENDDATUM_FIELD = listOf("sendDate", "verzenddatum")
        val CREATIEDATUM_FIELD = listOf("creationDate", "creatiedatum")
        val FORMAAT_FIELD = listOf("contentType", "formaat")
        val TREFWOORDEN_FIELD = listOf("trefwoorden")

        val logger = KotlinLogging.logger { }

        fun findConfigurationByUrl(url: URI) = { properties: JsonNode ->
            url.toString().startsWith(properties[URL_PROPERTY].textValue())
        }
    }
}

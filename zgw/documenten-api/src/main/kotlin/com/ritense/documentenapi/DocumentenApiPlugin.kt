/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.documentenapi.client.ConfidentialityLevel
import com.ritense.documentenapi.client.CreateDocumentRequest
import com.ritense.documentenapi.client.DocumentStatusType
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.event.DocumentCreated
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.service.TemporaryResourceStorageService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.context.ApplicationEventPublisher
import java.io.InputStream
import java.net.URI
import java.time.LocalDate

@Plugin(
    key = "documentenapi",
    title = "Documenten API",
    description = "Connects to the Documenten API to store documents"
)
class DocumentenApiPlugin(
    val client: DocumentenApiClient,
    val storageService: TemporaryResourceStorageService,
    val applicationEventPublisher: ApplicationEventPublisher
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "bronorganisatie", secret = false)
    lateinit var bronorganisatie: String

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: DocumentenApiAuthentication

    @PluginAction(
        key = "store-temp-document",
        title = "Store temporary document",
        description = "Store a temporary document in the Documenten API",
        activityTypes = [ActivityType.SERVICE_TASK]
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
        val documentLocation = execution.getVariable(localDocumentLocation) as String
        val contentAsInputStream = storageService.getResourceContentAsInputStream(documentLocation)
        val metadata = storageService.getResourceMetadata(documentLocation)
        val fileNameNotNull = fileName ?: metadata[MetadataType.FILE_NAME.key] as String

        storeDocument(
            execution = execution,
            metadata = metadata,
            title = title ?: fileNameNotNull,
            confidentialityLevel = getConfidentialityLevel(confidentialityLevel),
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
        activityTypes = [ActivityType.SERVICE_TASK]
    )
    fun storeUploadedDocument(
        execution: DelegateExecution
    ) {
        val resourceId = execution.getVariable(RESOURCE_ID_PROCESS_VAR) as String
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
            storedDocumentUrl = DOCUMENT_URL_PROCESS_VAR
        )
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
        storedDocumentUrl: String
    ) {
        val request = CreateDocumentRequest(
            bronorganisatie = bronorganisatie,
            creatiedatum = getLocalDateFromMetaData(metadata, "creationDate", LocalDate.now())!!,
            titel = title,
            vertrouwelijkheidaanduiding = getConfidentialityLevel(confidentialityLevel),
            auteur = metadata["author"] as String? ?: DEFAULT_AUTHOR,
            status = status,
            taal = language ?: DEFAULT_LANGUAGE,
            bestandsnaam = filename,
            inhoud = contentAsInputStream,
            beschrijving = description,
            ontvangstdatum = getLocalDateFromMetaData(metadata, "receiptDate"),
            verzenddatum = getLocalDateFromMetaData(metadata, "sendDate"),
            informatieobjecttype = informationObjectType,
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

    private fun getConfidentialityLevel(confidentialityLevel: String?): String? {
        return if (confidentialityLevel == null) {
            null
        } else {
            ConfidentialityLevel.fromKey(confidentialityLevel).key
        }
    }

    private fun getStatusFromMetaData(metadata: Map<String, Any>): DocumentStatusType {
        val status = metadata["status"] as String?
        return if (status != null) {
            DocumentStatusType.fromKey(status)
        } else {
            DocumentStatusType.DEFINITIEF
        }
    }

    private fun getFilenameFromMetaData(metadata: Map<String, Any>): String? {
        return metadata["filename"] as String? ?: metadata[MetadataType.FILE_NAME.name] as String?
    }

    companion object {
        const val DEFAULT_AUTHOR = "GZAC"
        const val DEFAULT_LANGUAGE = "nld"
        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val DOCUMENT_URL_PROCESS_VAR = "documentUrl"
    }
}

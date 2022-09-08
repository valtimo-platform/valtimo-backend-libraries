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

import com.ritense.documentenapi.client.ConfidentialityNotice
import com.ritense.documentenapi.client.CreateDocumentRequest
import com.ritense.documentenapi.client.DocumentStatusType
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.event.DocumentCreated
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.resource.service.TemporaryResourceStorageService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.context.ApplicationEventPublisher
import java.net.URI

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
        @PluginActionProperty fileName: String,
        @PluginActionProperty confidentialityNotice: String,
        @PluginActionProperty title: String,
        @PluginActionProperty description: String,
        @PluginActionProperty localDocumentLocation: String,
        @PluginActionProperty storedDocumentUrl: String,
        @PluginActionProperty informatieobjecttype: String,
        @PluginActionProperty taal: String = "nld",
        @PluginActionProperty status: DocumentStatusType = DocumentStatusType.DEFINITIEF
    ) {
        val documentLocation = execution.getVariable(localDocumentLocation) as String
        val contentAsInputStream = storageService.getResourceContentAsInputStream(documentLocation)

        val request = CreateDocumentRequest(
            bronorganisatie = bronorganisatie,
            titel = title,
            vertrouwelijkheidaanduiding = ConfidentialityNotice.fromKey(confidentialityNotice).key,
            status = status,
            taal = taal,
            bestandsnaam = fileName,
            inhoud = contentAsInputStream,
            beschrijving = description,
            informatieobjecttype = informatieobjecttype,
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
}

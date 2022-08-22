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

package com.ritense.zakenapi

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openapi.example.api.ZaakinformatieobjectenApi
import org.openapi.example.invoker.ApiClient
import org.openapi.example.model.ZaakInformatieObject
import java.net.URI
import java.util.UUID

@Plugin(
    key = "zakenapi",
    title = "Zaken API",
    description = "Connects to the Zaken API"
)
class ZakenApiPlugin(
    val client: ZakenApiClient,
    val zaakUrlProvider: ZaakUrlProvider,
    val resourceProvider: ResourceProvider,
    val documentService: DocumentService,
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: String
    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: ZakenApiAuthentication

    @PluginAction(
        key = "link-document-to-zaak",
        title = "Link Documenten API document to Zaak",
        description = "Stores a link to an existing document in the Documenten API with a Zaak",
        activityTypes = [ActivityType.SERVICE_TASK]
    )
    fun linkDocumentToZaak(
        execution: DelegateExecution,
        @PluginActionProperty documentUrl: String,
        @PluginActionProperty titel: String?,
        @PluginActionProperty beschrijving: String?
    ){
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaak(documentId)

        val webclientWIthFilter = client.webclient
            .mutate()
            .filter(authenticationPluginConfiguration)
            .build()
        val apiClient = ApiClient(webclientWIthFilter)

        val api = ZaakinformatieobjectenApi(apiClient)
        val zaakRequest = ZaakInformatieObject().apply {
            this.informatieobject = URI(documentUrl)
            this.zaak = URI(zaakUrl)
            this.titel = titel
            this.beschrijving = beschrijving
        }

        api.zaakinformatieobjectCreate(
            zaakRequest,
            null,
            null
        ).block()

        val resource = resourceProvider.getResource(documentUrl)
        documentService.assignResource(
            JsonSchemaDocumentId.existingId(documentId),
            resource.id(),
            mapOf("createInformatieObject" to false)
        )
    }
}
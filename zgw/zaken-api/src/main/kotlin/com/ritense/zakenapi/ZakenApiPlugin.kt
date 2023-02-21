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

package com.ritense.zakenapi

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakInstanceLinkId
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.domain.rol.BetrokkeneType
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zgw.Page
import com.ritense.zgw.Rsin
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@Plugin(
    key = ZakenApiPlugin.PLUGIN_KEY,
    title = "Zaken API",
    description = "Connects to the Zaken API"
)
class ZakenApiPlugin(
    private val client: ZakenApiClient,
    private val zaakUrlProvider: ZaakUrlProvider,
    private val resourceProvider: ResourceProvider,
    private val documentService: DocumentService,
    private val storageService: TemporaryResourceStorageService,
    private val zaakInstanceLinkRepository: ZaakInstanceLinkRepository,
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: ZakenApiAuthentication

    @PluginAction(
        key = "link-document-to-zaak",
        title = "Link Documenten API document to Zaak",
        description = "Stores a link to an existing document in the Documenten API with a Zaak",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun linkDocumentToZaak(
        execution: DelegateExecution,
        @PluginActionProperty documentUrl: String,
        @PluginActionProperty titel: String?,
        @PluginActionProperty beschrijving: String?
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaak(documentId)

        val request = LinkDocumentRequest(
            documentUrl,
            zaakUrl,
            titel,
            beschrijving
        )

        linkDocument(documentId, request, documentUrl)
    }

    @PluginAction(
        key = "link-uploaded-document-to-zaak",
        title = "Link Uploaded Documenten API document to Zaak",
        description = "Stores a link to an uploaded document in the Documenten API with a Zaak",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun linkUploadedDocumentToZaak(
        execution: DelegateExecution
    ) {
        val documentUrl = execution.getVariable(DOCUMENT_URL_PROCESS_VAR) as String
        val resourceId = execution.getVariable(RESOURCE_ID_PROCESS_VAR) as String
        val metadata = storageService.getResourceMetadata(resourceId)

        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaak(documentId)

        val request = LinkDocumentRequest(
            documentUrl,
            zaakUrl,
            metadata["title"] as String?,
            metadata["description"] as String?,
        )
        linkDocument(documentId, request, documentUrl)
    }

    @PluginAction(
        key = "create-zaak",
        title = "Create zaak",
        description = "Creates a zaak in the Zaken API",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun createZaak(
        execution: DelegateExecution,
        @PluginActionProperty rsin: Rsin,
        @PluginActionProperty zaaktypeUrl: URI,
    ) {
        val documentId = UUID.fromString(execution.businessKey)

        val zaak = client.createZaak(
            authenticationPluginConfiguration,
            url,
            CreateZaakRequest(
                bronorganisatie = rsin,
                zaaktype = zaaktypeUrl,
                verantwoordelijkeOrganisatie = rsin,
                startdatum = LocalDate.now()
            )
        )

        zaakInstanceLinkRepository.save(
            ZaakInstanceLink(
                ZaakInstanceLinkId(UUID.randomUUID()),
                zaak.url,
                zaak.uuid,
                documentId,
                zaak.zaaktype
            )
        )
    }

    @PluginAction(
        key = "create-natuurlijk-persoon-zaak-rol",
        title = "Create natuurlijk persoon zaakrol",
        description = "Adds a zaakrol to the zaak in the Zaken API",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun createNatuurlijkPersoonZaakRol(
        execution: DelegateExecution,
        @PluginActionProperty roltypeUrl: String,
        @PluginActionProperty rolToelichting: String,
        @PluginActionProperty inpBsn: String?,
        @PluginActionProperty anpIdentificatie: String?,
        @PluginActionProperty inpA_nummer: String?
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaak(documentId)

        client.createZaakRol(
            authenticationPluginConfiguration,
            url,
            Rol(
                zaak = URI(zaakUrl),
                roltype = URI(roltypeUrl),
                roltoelichting = rolToelichting,
                betrokkeneType = BetrokkeneType.NATUURLIJK_PERSOON,
                betrokkeneIdentificatie = RolNatuurlijkPersoon(
                    inpBsn = inpBsn,
                    anpIdentificatie = anpIdentificatie,
                    inpA_nummer = inpA_nummer
                )
            )
        )

    }

    private fun linkDocument(documentId: UUID, request: LinkDocumentRequest, documentUrl: String) {
        client.linkDocument(authenticationPluginConfiguration, url, request)
        val resource = resourceProvider.getResource(documentUrl)
        documentService.assignResource(
            JsonSchemaDocumentId.existingId(documentId),
            resource.id(),
            mapOf("createInformatieObject" to false)
        )
    }

    fun getZaakObjecten(zaakUrl: URI): List<ZaakObject> {
        var currentPage = 1
        var currentResults: Page<ZaakObject>?
        val results = mutableListOf<ZaakObject>()

        do {
            currentResults = client.getZaakObjecten(
                authenticationPluginConfiguration,
                url,
                zaakUrl,
                currentPage++
            )
            results.addAll(currentResults.results)
        } while (currentResults?.next != null)

        return results
    }

    fun getZaakRollen(zaakUrl: URI, roleType: RolType? = null): List<Rol> {
        var next = true

        return generateSequence(1) { i -> if (next) i + 1 else null }
            .flatMap { pageNumber ->
                val result = client.getZaakRollen(authenticationPluginConfiguration,
                    url,
                    zaakUrl,
                    pageNumber,
                    roleType)

                if (result.next == null) {
                    next = false
                }

                result.results
            }.toList()
    }

    companion object {
        const val PLUGIN_KEY = "zakenapi"
        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val DOCUMENT_URL_PROCESS_VAR = "documentUrl"
    }
}

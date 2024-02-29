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

package com.ritense.zakenapi

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.catalogiapi.CatalogiApiPlugin
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.processlink.domain.ActivityTypeWithEventName.USER_TASK_CREATE
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.contract.validation.Url
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.CreateZaakResultaatRequest
import com.ritense.zakenapi.domain.CreateZaakStatusRequest
import com.ritense.zakenapi.domain.CreateZaakeigenschapRequest
import com.ritense.zakenapi.domain.Opschorting
import com.ritense.zakenapi.domain.PatchZaakRequest
import com.ritense.zakenapi.domain.UpdateZaakeigenschapRequest
import com.ritense.zakenapi.domain.Verlenging
import com.ritense.zakenapi.domain.ZaakHersteltermijn
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakInstanceLinkId
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.domain.ZaakStatus
import com.ritense.zakenapi.domain.ZaakopschortingRequest
import com.ritense.zakenapi.domain.rol.BetrokkeneType
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.repository.ZaakHersteltermijnRepository
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zgw.Page
import com.ritense.zgw.Rsin
import mu.KLogger
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.util.UUID

@Plugin(
    key = ZakenApiPlugin.PLUGIN_KEY,
    title = "Zaken API",
    description = "Connects to the Zaken API"
)
class ZakenApiPlugin(
    private val client: ZakenApiClient,
    private val zaakUrlProvider: ZaakUrlProvider,
    private val storageService: TemporaryResourceStorageService,
    private val zaakInstanceLinkRepository: ZaakInstanceLinkRepository,
    private val pluginService: PluginService,
    private val zaakHersteltermijnRepository: ZaakHersteltermijnRepository,
    private val platformTransactionManager: PlatformTransactionManager
) {
    @Url
    @PluginProperty(key = URL_PROPERTY, secret = false)
    lateinit var url: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: ZakenApiAuthentication

    @PluginAction(
        key = "link-document-to-zaak",
        title = "Link Documenten API document to Zaak",
        description = "Stores a link to an existing document in the Documenten API with a Zaak",
        activityTypes = [SERVICE_TASK_START]
    )
    fun linkDocumentToZaak(
        execution: DelegateExecution,
        @PluginActionProperty documentUrl: String,
        @PluginActionProperty titel: String?,
        @PluginActionProperty beschrijving: String?
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        val request = LinkDocumentRequest(
            documentUrl,
            zaakUrl.toString(),
            titel,
            beschrijving
        )

        client.linkDocument(authenticationPluginConfiguration, url, request)
    }

    @PluginAction(
        key = "link-uploaded-document-to-zaak",
        title = "Link Uploaded Documenten API document to Zaak",
        description = "Stores a link to an uploaded document in the Documenten API with a Zaak",
        activityTypes = [SERVICE_TASK_START]
    )
    fun linkUploadedDocumentToZaak(
        execution: DelegateExecution
    ) {
        val documentUrl = execution.getVariable(DOCUMENT_URL_PROCESS_VAR) as String
        val resourceId = execution.getVariable(RESOURCE_ID_PROCESS_VAR) as String
        val metadata = storageService.getResourceMetadata(resourceId)

        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        val request = LinkDocumentRequest(
            documentUrl,
            zaakUrl.toString(),
            metadata["title"] as String?,
            metadata["description"] as String?,
        )
        client.linkDocument(authenticationPluginConfiguration, url, request)
    }

    @PluginAction(
        key = "create-zaak",
        title = "Create zaak",
        description = "Creates a zaak in the Zaken API",
        activityTypes = [SERVICE_TASK_START]
    )
    fun createZaak(
        execution: DelegateExecution,
        @PluginActionProperty rsin: Rsin,
        @PluginActionProperty zaaktypeUrl: URI,
    ) {
        val documentId = UUID.fromString(execution.businessKey)

        createZaak(documentId, rsin, zaaktypeUrl)
    }

    fun createZaak(
        documentId: UUID,
        rsin: Rsin,
        zaaktypeUrl: URI,
    ) {
        val zaakInstanceLink = zaakInstanceLinkRepository.findByDocumentId(documentId)
        if (zaakInstanceLink != null) {
            logger.warn { "SKIPPING ZAAK CREATION. Reason: a zaak already exists for this case. Case id '$documentId'. Zaak URL '${zaakInstanceLink.zaakInstanceUrl}'." }
            return
        }

        val startdatum = LocalDate.now()
        val uiterlijkeEinddatumAfdoening = calculateUiterlijkeEinddatumAfdoening(zaaktypeUrl, startdatum)

        val zaak = client.createZaak(
            authenticationPluginConfiguration,
            url,
            CreateZaakRequest(
                bronorganisatie = rsin,
                zaaktype = zaaktypeUrl,
                verantwoordelijkeOrganisatie = rsin,
                startdatum = startdatum,
                uiterlijkeEinddatumAfdoening = uiterlijkeEinddatumAfdoening,
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
        activityTypes = [SERVICE_TASK_START]
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
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        client.createZaakRol(
            authenticationPluginConfiguration,
            url,
            Rol(
                zaak = zaakUrl,
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

    @PluginAction(
        key = "create-niet-natuurlijk-persoon-zaak-rol",
        title = "Create niet-natuurlijk persoon zaakrol",
        description = "Adds a zaakrol to the zaak in the Zaken API",
        activityTypes = [SERVICE_TASK_START]
    )
    fun createNietNatuurlijkPersoonZaakRol(
        execution: DelegateExecution,
        @PluginActionProperty roltypeUrl: String,
        @PluginActionProperty rolToelichting: String,
        @PluginActionProperty innNnpId: String?,
        @PluginActionProperty annIdentificatie: String?
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        client.createZaakRol(
            authenticationPluginConfiguration,
            url,
            Rol(
                zaak = zaakUrl,
                roltype = URI(roltypeUrl),
                roltoelichting = rolToelichting,
                betrokkeneType = BetrokkeneType.NIET_NATUURLIJK_PERSOON,
                betrokkeneIdentificatie = RolNietNatuurlijkPersoon(
                    annIdentificatie = annIdentificatie,
                    innNnpId = innNnpId
                )
            )
        )
    }

    @PluginAction(
        key = "set-zaakstatus",
        title = "Set zaak status",
        description = "Sets the status of a zaak",
        activityTypes = [SERVICE_TASK_START]
    )
    fun setZaakStatus(
        execution: DelegateExecution,
        @PluginActionProperty statustypeUrl: URI,
        @PluginActionProperty statustoelichting: String?,
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        client.createZaakStatus(
            authenticationPluginConfiguration,
            url,
            CreateZaakStatusRequest(
                zaak = zaakUrl,
                statustype = statustypeUrl,
                datumStatusGezet = LocalDateTime.now().minusSeconds(5),
                statustoelichting = statustoelichting,
            )
        )
    }

    @PluginAction(
        key = "create-zaakresultaat",
        title = "Create zaak status",
        description = "Creates a resultaat for a zaak",
        activityTypes = [SERVICE_TASK_START]
    )
    fun createZaakResultaat(
        execution: DelegateExecution,
        @PluginActionProperty resultaattypeUrl: URI,
        @PluginActionProperty toelichting: String?,
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        client.createZaakResultaat(
            authenticationPluginConfiguration,
            url,
            CreateZaakResultaatRequest(
                zaak = zaakUrl,
                resultaattype = resultaattypeUrl,
                toelichting = toelichting,
            )
        )
    }

    @PluginAction(
        key = "set-zaakopschorting",
        title = "Set case suspension",
        description = "Suspends a case, sets the suspend status to true and adds a duration of time to the planned end date",
        activityTypes = [SERVICE_TASK_START]
    )
    fun setZaakOpschorting(
        execution: DelegateExecution,
        @PluginActionProperty verlengingsduur: String,
        @PluginActionProperty toelichtingVerlenging: String,
        @PluginActionProperty toelichtingOpschorting: String,
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        client.setZaakOpschorting(
            authenticationPluginConfiguration,
            zaakUrl,
            ZaakopschortingRequest(
                verlenging = Verlenging(
                    reden = toelichtingVerlenging,
                    duur = "P$verlengingsduur" + "D"
                ),
                opschorting = Opschorting(
                    indicatie = true.toString(),
                    reden = toelichtingOpschorting
                )
            )
        )
    }

    @PluginAction(
        key = "start-hersteltermijn",
        title = "Start hersteltermijn",
        description = "Start the recovery period for a case",
        activityTypes = [SERVICE_TASK_START, USER_TASK_CREATE]
    )
    fun startHersteltermijn(
        execution: DelegateExecution,
        @PluginActionProperty maxDurationInDays: Int,
    ) {
        TransactionTemplate(platformTransactionManager).executeWithoutResult {
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
            val startDate = LocalDate.now()
            val hersteltermijn = ZaakHersteltermijn(
                zaakUrl = zaakUrl,
                startDate = startDate,
                maxDurationInDays = maxDurationInDays
            )

            val existingHerseltermijn = zaakHersteltermijnRepository.findByZaakUrlAndEndDateIsNull(zaakUrl)
            check(existingHerseltermijn == null || existingHerseltermijn != hersteltermijn) { "Hersteltermijn already exists for zaak '$zaakUrl'. " }

            if (existingHerseltermijn == null) {
                val zaak = client.getZaak(authenticationPluginConfiguration, zaakUrl)
                val uiterlijkeEinddatumAfdoening = zaak.uiterlijkeEinddatumAfdoening
                    ?: calculateUiterlijkeEinddatumAfdoening(zaak.zaaktype, zaak.startdatum)
                require(uiterlijkeEinddatumAfdoening != null) { "No 'uiterlijkeEinddatumAfdoening' available for zaak '$zaakUrl' " }

                client.patchZaak(
                    authenticationPluginConfiguration, url, zaakUrl, PatchZaakRequest(
                        uiterlijkeEinddatumAfdoening = uiterlijkeEinddatumAfdoening.plusDays(maxDurationInDays.toLong())
                    )
                )
                zaakHersteltermijnRepository.save(hersteltermijn)
            }
        }
    }

    @PluginAction(
        key = "end-hersteltermijn",
        title = "End hersteltermijn",
        description = "End the recovery period for a case",
        activityTypes = [SERVICE_TASK_START]
    )
    fun endHersteltermijn(
        execution: DelegateExecution,
    ) {
        TransactionTemplate(platformTransactionManager).executeWithoutResult {
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
            val endDate = LocalDate.now()
            val herseltermijn = zaakHersteltermijnRepository.findByZaakUrlAndEndDateIsNull(zaakUrl)
                ?: throw IllegalStateException("Hersteltermijn doesn't exists for zaak '$zaakUrl'. ")
            val updatedHersteltermijn = herseltermijn.copy(endDate = endDate)

            val uiterlijkeEinddatumAfdoening =
                client.getZaak(authenticationPluginConfiguration, zaakUrl).uiterlijkeEinddatumAfdoening
            if (uiterlijkeEinddatumAfdoening != null) {
                val newUiterlijkeEinddatumAfdoening = uiterlijkeEinddatumAfdoening.minusDays(
                    herseltermijn.maxDurationInDays.toLong() - herseltermijn.startDate.until(
                        endDate,
                        DAYS
                    )
                )
                client.patchZaak(
                    authenticationPluginConfiguration, url, zaakUrl, PatchZaakRequest(
                        uiterlijkeEinddatumAfdoening = newUiterlijkeEinddatumAfdoening
                    )
                )
            }

            zaakHersteltermijnRepository.save(updatedHersteltermijn)
        }
    }

    @PluginAction(
        key = "create-zaakeigenschap",
        title = "Create zaakeigenschap",
        description = "Creates a zaakeigenschap",
        activityTypes = [SERVICE_TASK_START]
    )
    fun createZaakeigenschap(
        execution: DelegateExecution,
        @PluginActionProperty eigenschapUrl: URI,
        @PluginActionProperty eigenschapValue: String,
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
        val request = CreateZaakeigenschapRequest(zaakUrl, eigenschapUrl, eigenschapValue)

        client.createZaakeigenschap(authenticationPluginConfiguration, url, request)
    }

    @PluginAction(
        key = "update-zaakeigenschap",
        title = "Update zaakeigenschap",
        description = "Updates a zaakeigenschap",
        activityTypes = [SERVICE_TASK_START]
    )
    fun updateZaakeigenschap(
        execution: DelegateExecution,
        @PluginActionProperty eigenschapUrl: URI,
        @PluginActionProperty eigenschapValue: String,
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
        val zaakeigenschappen = client.getZaakeigenschappen(authenticationPluginConfiguration, url, zaakUrl)
            .filter { it.eigenschap == eigenschapUrl }
        require(zaakeigenschappen.isNotEmpty()) { "No zaakeigenschap exist for zaak '$zaakUrl' and eigenschap '$eigenschapUrl'" }
        zaakeigenschappen.forEach { zaakeigenschap ->
            if (zaakeigenschap.waarde != eigenschapValue) {
                val request = UpdateZaakeigenschapRequest(zaakUrl, eigenschapUrl, eigenschapValue)
                client.updateZaakeigenschap(authenticationPluginConfiguration, url, zaakeigenschap.url, request)
            }
        }
    }

    @PluginAction(
        key = "delete-zaakeigenschap",
        title = "Create zaakeigenschap",
        description = "Creates a zaakeigenschap",
        activityTypes = [SERVICE_TASK_START]
    )
    fun deleteZaakeigenschap(
        execution: DelegateExecution,
        @PluginActionProperty eigenschapUrl: URI,
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
        client.getZaakeigenschappen(authenticationPluginConfiguration, url, zaakUrl)
            .filter { it.eigenschap == eigenschapUrl }
            .forEach { client.deleteZaakeigenschap(authenticationPluginConfiguration, url, it.url) }
    }

    fun getZaakInformatieObjecten(zaakUrl: URI): List<ZaakInformatieObject> {
        return client.getZaakInformatieObjecten(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            zaakUrl = zaakUrl
        )
    }

    fun getZaakInformatieObjectenByInformatieobjectUrl(informatieobjectUrl: URI): List<ZaakInformatieObject> {
        return client.getZaakInformatieObjecten(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            informatieobjectUrl = informatieobjectUrl
        )
    }

    fun deleteZaakInformatieobject(zaakInformatieobjectUrl: URI) {
        return client.deleteZaakInformatieObject(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            zaakInformatieobjectUrl = zaakInformatieobjectUrl
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
        return Page.getAll(100) { page ->
            client.getZaakRollen(
                authenticationPluginConfiguration,
                url, zaakUrl, page, roleType
            )
        }
    }

    fun getZaakStatus(zaakUrl: URI): ZaakStatus? {
        val zaak = getZaak(zaakUrl)
        return if (zaak.status == null) {
            null
        } else {
            client.getZaakStatus(authenticationPluginConfiguration, URI(zaak.status))
        }
    }

    fun getZaak(zaakUrl: URI): ZaakResponse {
        return client.getZaak(authenticationPluginConfiguration, zaakUrl)
    }

    private fun calculateUiterlijkeEinddatumAfdoening(zaaktypeUrl: URI, startdatum: LocalDate): LocalDate? {
        return getCatalogiApiPlugin(zaaktypeUrl)
            ?.getZaaktype(zaaktypeUrl)
            ?.doorlooptijd
            ?.let { doorlooptijd -> startdatum.atStartOfDay() + doorlooptijd }
            ?.toLocalDate()
    }

    private fun getCatalogiApiPlugin(zaakTypeUrl: URI): CatalogiApiPlugin? {
        return pluginService.createInstance(
            CatalogiApiPlugin::class.java,
            CatalogiApiPlugin.findConfigurationByUrl(zaakTypeUrl)
        )
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PLUGIN_KEY = "zakenapi"
        const val URL_PROPERTY = "url"
        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val DOCUMENT_URL_PROCESS_VAR = "documentUrl"
        fun findConfigurationByUrl(url: URI) =
            { properties: JsonNode -> url.toString().startsWith(properties.get(URL_PROPERTY).textValue()) }
    }
}

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

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.ritense.authorization.AuthorizationContext
import com.ritense.catalogiapi.CatalogiApiPlugin
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentAssociationService
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
import com.ritense.zakenapi.domain.ZaakResultaat
import com.ritense.zakenapi.domain.ZaakStatus
import com.ritense.zakenapi.domain.ZaakopschortingRequest
import com.ritense.zakenapi.domain.rol.BetrokkeneType
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.repository.ZaakHersteltermijnRepository
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zgw.LoggingConstants.CATALOGI_API
import com.ritense.zgw.LoggingConstants.DOCUMENTEN_API
import com.ritense.zgw.Page
import com.ritense.zgw.Rsin
import mu.KLogger
import mu.KotlinLogging
import mu.withLoggingContext
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
    private val platformTransactionManager: PlatformTransactionManager,
    private val documentService: DocumentService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
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
        withLoggingContext(
            DOCUMENTEN_API.ENKELVOUDIG_INFORMATIE_OBJECT to documentUrl
        ) {
            logger.debug { "Starting to link document with URL '$documentUrl' to zaak" }

            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

            val request = LinkDocumentRequest(
                documentUrl,
                zaakUrl.toString(),
                titel,
                beschrijving
            )

            client.linkDocument(authenticationPluginConfiguration, url, request)

            logger.info { "Document with URL '$documentUrl' linked successfully to zaak with URL '$zaakUrl'" }
        }
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
        logger.debug { "Starting to link uploaded document to zaak." }
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
        logger.info { "Linked uploaded document with URL '$documentUrl' to zaak with URL '$zaakUrl'" }
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
        @PluginActionProperty description: String? = null,
        @PluginActionProperty plannedEndDate: String? = null,
        @PluginActionProperty finalDeliveryDate: String? = null,
    ) {
        withLoggingContext(
            CATALOGI_API.ZAAKTYPE to zaaktypeUrl.toString()
        ) {
            val documentId = UUID.fromString(execution.businessKey)

            createZaak(
                documentId,
                rsin,
                zaaktypeUrl,
                description,
                plannedEndDate?.let { LocalDate.parse(it) },
                finalDeliveryDate?.let { LocalDate.parse(it) },
            )

            logger.info { "Zaak of zaaktype with URL '$zaaktypeUrl' created for document with id '$documentId'" }
        }
    }

    fun createZaak(
        documentId: UUID,
        rsin: Rsin,
        zaaktypeUrl: URI,
        description: String? = null,
        plannedEndDate: LocalDate? = null,
        finalDeliveryDate: LocalDate? = null,
    ) {
        withLoggingContext(
            CATALOGI_API.ZAAKTYPE to zaaktypeUrl.toString(),
            "com.ritense.document.domain.impl.JsonSchemaDocument" to documentId.toString(),
        ) {
            logger.debug { "Starting creation of zaak of zaaktype with URL '$zaaktypeUrl' for document with id '$documentId'" }
            val zaakInstanceLink = zaakInstanceLinkRepository.findByDocumentId(documentId)

            if (zaakInstanceLink != null) {
                logger.warn { "Skipping zaak creation. Zaak already exists for document with id '$documentId'. Zaak URL: '${zaakInstanceLink.zaakInstanceUrl}'." }
                return
            }

            val startdatum = LocalDate.now()
            val uiterlijkeEinddatumAfdoening =
                finalDeliveryDate ?: calculateUiterlijkeEinddatumAfdoening(zaaktypeUrl, startdatum)

            val zaak = client.createZaak(
                authenticationPluginConfiguration,
                url,
                CreateZaakRequest(
                    bronorganisatie = rsin,
                    zaaktype = zaaktypeUrl,
                    verantwoordelijkeOrganisatie = rsin,
                    startdatum = startdatum,
                    uiterlijkeEinddatumAfdoening = uiterlijkeEinddatumAfdoening,
                    omschrijving = description,
                    einddatumGepland = plannedEndDate,
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

            logger.info { "Zaak with URL '${zaak.url}' created successfully for document with id '$documentId''" }
        }
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
        withLoggingContext(
            CATALOGI_API.ROLTYPE to roltypeUrl,
        ) {
            logger.debug { "Creating natuurlijk persoon zaakrol with roltype URL '$roltypeUrl' for document with id '${execution.businessKey}'" }

            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

            val rol = client.createZaakRol(
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

            logger.info { "Natuurlijk persoon zaakrol with URL '${rol.url}' created for document with id '$documentId' and zaak with URL '$zaakUrl'." }
        }
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
        withLoggingContext(
            CATALOGI_API.ROLTYPE to roltypeUrl,
        ) {
            logger.debug { "Creating niet-natuurlijk persoon zaakrol with roltype URL '$roltypeUrl' for document with id '${execution.businessKey}'" }

            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

            val rol = client.createZaakRol(
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

            logger.info { "Niet-natuurlijk persoon zaakrol with URL '${rol.url}' created for document with id '$documentId' and zaak with URL '$zaakUrl'." }
        }
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
        withLoggingContext(
            CATALOGI_API.STATUSTYPE to statustypeUrl.toString(),
        ) {
            logger.debug { "Setting zaak status with type URL '$statustypeUrl' for document with id '${execution.businessKey}'" }
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

            logger.info { "Zaak status with type URL '$statustypeUrl' set successfully for zaak with URL '$zaakUrl'" }
        }
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
        withLoggingContext(
            CATALOGI_API.RESULTAATTYPE to resultaattypeUrl.toString(),
        ) {
            logger.debug { "Creating zaak resultaat with type URL '$resultaattypeUrl' for document with id '${execution.businessKey}'" }
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

            val zaakResultaat = client.createZaakResultaat(
                authenticationPluginConfiguration,
                url,
                CreateZaakResultaatRequest(
                    zaak = zaakUrl,
                    resultaattype = resultaattypeUrl,
                    toelichting = toelichting,
                )
            )

            logger.info { "Zaak resultaat with URL '${zaakResultaat.url}' created successfully for document with id '$documentId' and zaak with URL '$zaakUrl'" }
        }
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
        logger.debug { "Setting zaak opschorting for document with id '${execution.businessKey}'" }
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        val zaakOpschorting = client.setZaakOpschorting(
            authenticationPluginConfiguration,
            zaakUrl,
            ZaakopschortingRequest(
                verlenging = Verlenging(
                    reden = toelichtingVerlenging,
                    duur = "P$verlengingsduur" + "D"
                ),
                opschorting = Opschorting(
                    indicatie = true,
                    reden = toelichtingOpschorting
                )
            )
        )
        logger.info { "Zaak opschorting with url '${zaakOpschorting.url}' set successfully for zaak with URL '$zaakUrl' and document with id '${documentId}'" }
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
        logger.debug { "Setting hersteltermijn for document with id '${execution.businessKey}'" }

        TransactionTemplate(platformTransactionManager).executeWithoutResult {
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
            val startDate = LocalDate.now()
            val hersteltermijn = ZaakHersteltermijn(
                zaakUrl = zaakUrl,
                startDate = startDate,
                maxDurationInDays = maxDurationInDays
            )

            require(zaakHersteltermijnRepository.findByZaakUrlAndEndDateIsNull(zaakUrl) == null) {
                "Hersteltermijn already exists for zaak '$zaakUrl'"
            }

            val zaak = client.getZaak(authenticationPluginConfiguration, zaakUrl)
            val uiterlijkeEinddatumAfdoening = zaak.uiterlijkeEinddatumAfdoening
                ?: calculateUiterlijkeEinddatumAfdoening(zaak.zaaktype, zaak.startdatum)
            require(uiterlijkeEinddatumAfdoening != null) { "No 'uiterlijkeEinddatumAfdoening' available for zaak '$zaakUrl' " }
            require(zaak.opschorting == null || !zaak.opschorting.indicatie) { "Can't start recovery period for a suspended zaak" }

            val patchedZaak = client.patchZaak(
                authenticationPluginConfiguration, url, zaakUrl, PatchZaakRequest(
                    uiterlijkeEinddatumAfdoening = uiterlijkeEinddatumAfdoening.plusDays(maxDurationInDays.toLong()),
                    opschorting = Opschorting(true, "hersteltermijn")
                )
            )

            zaakHersteltermijnRepository.save(hersteltermijn)

            logger.info { "Opschorting because of hersteltermijn set for zaak with URL '${zaak.url}' with updated due date '${patchedZaak.uiterlijkeEinddatumAfdoening}'" }
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
        logger.debug { "Ending hersteltermijn for document with id '${execution.businessKey}'" }

        TransactionTemplate(platformTransactionManager).executeWithoutResult {
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
            val endDate = LocalDate.now()
            val herseltermijn = zaakHersteltermijnRepository.findByZaakUrlAndEndDateIsNull(zaakUrl)
                ?: throw IllegalStateException("Hersteltermijn doesn't exists for zaak '$zaakUrl'. ")
            val updatedHersteltermijn = herseltermijn.copy(endDate = endDate)

            val zaak = client.getZaak(authenticationPluginConfiguration, zaakUrl)
            if (zaak.uiterlijkeEinddatumAfdoening != null) {
                val newUiterlijkeEinddatumAfdoening = zaak.uiterlijkeEinddatumAfdoening.minusDays(
                    herseltermijn.maxDurationInDays.toLong() - herseltermijn.startDate.until(
                        endDate,
                        DAYS
                    )
                )
                client.patchZaak(
                    authenticationPluginConfiguration, url, zaakUrl, PatchZaakRequest(
                        uiterlijkeEinddatumAfdoening = newUiterlijkeEinddatumAfdoening,
                        opschorting = Opschorting(false, "")
                    )
                )
            }

            zaakHersteltermijnRepository.save(updatedHersteltermijn)

            logger.info { "Hersteltermijn ended for zaak with URL '${zaak.url}' and document with id '${documentId}'" }
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
        withLoggingContext(
            CATALOGI_API.EIGENSCHAP to eigenschapUrl.toString(),
        ) {
            logger.debug { "Creating zaakeigenschap with URL '$eigenschapUrl' for document with id '${execution.businessKey}'" }
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
            val request = CreateZaakeigenschapRequest(zaakUrl, eigenschapUrl, eigenschapValue)

            client.createZaakeigenschap(authenticationPluginConfiguration, url, request)
            logger.info { "Zaakeigenschap with URL '$eigenschapUrl' created for zaak with URL '$zaakUrl' and document with id '${documentId}'" }
        }
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
        withLoggingContext(
            CATALOGI_API.EIGENSCHAP to eigenschapUrl.toString(),
        ) {
            logger.debug { "Updating zaakeigenschap with url '${eigenschapUrl}' for document with id '${execution.businessKey}'" }
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
            val zaakeigenschappen = client.getZaakeigenschappen(authenticationPluginConfiguration, url, zaakUrl)
                .filter { it.eigenschap == eigenschapUrl }
            require(zaakeigenschappen.isNotEmpty()) { "No zaakeigenschap exist for zaak '$zaakUrl' and eigenschap '$eigenschapUrl'" }
            zaakeigenschappen.forEach { zaakeigenschap ->
                if (zaakeigenschap.waarde != eigenschapValue) {
                    val request = UpdateZaakeigenschapRequest(zaakUrl, eigenschapUrl, eigenschapValue)
                    client.updateZaakeigenschap(authenticationPluginConfiguration, url, zaakeigenschap.url, request)
                    logger.info { "Zaakeigenschap with URL '${eigenschapUrl}' updated for zaak with URL '$zaakUrl' and document with id '${documentId}'" }
                }
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
        withLoggingContext(
            CATALOGI_API.EIGENSCHAP to eigenschapUrl.toString(),
        ) {
            logger.debug { "Deleting zaakeigenschap with url '${eigenschapUrl}' for document with id '${execution.businessKey}'" }
            val documentId = UUID.fromString(execution.businessKey)
            val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
            client.getZaakeigenschappen(authenticationPluginConfiguration, url, zaakUrl)
                .filter { it.eigenschap == eigenschapUrl }
                .forEach { client.deleteZaakeigenschap(authenticationPluginConfiguration, url, it.url) }
            logger.info { "Zaakeigenschap with URL '${eigenschapUrl}' deleted for zaak with URL '$zaakUrl' and document with id '${documentId}'" }
        }
    }

    fun getZaakInformatieObjecten(zaakUrl: URI): List<ZaakInformatieObject> {
        logger.debug { "Fetching zaak informatie objecten for zaak with URL '$zaakUrl'" }
        return client.getZaakInformatieObjecten(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            zaakUrl = zaakUrl
        )
    }

    fun getZaakInformatieObjectenByInformatieobjectUrl(informatieobjectUrl: URI): List<ZaakInformatieObject> {
        logger.debug { "Fetching zaak informatie objecten by informatieobject URL '$informatieobjectUrl'" }
        return client.getZaakInformatieObjecten(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            informatieobjectUrl = informatieobjectUrl
        )
    }

    fun deleteZaakInformatieobject(zaakInformatieobjectUrl: URI) {
        logger.debug { "Deleting zaak informatie object for URL '$zaakInformatieobjectUrl'" }
        return client.deleteZaakInformatieObject(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            zaakInformatieobjectUrl = zaakInformatieobjectUrl
        )
    }

    fun getZaakObjecten(zaakUrl: URI): List<ZaakObject> {
        logger.debug { "Fetching zaak objecten for zaak with URL '$zaakUrl'" }
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

        logger.debug { "Fetched ${results.size} zaak objecten for zaak with URL '$zaakUrl'" }
        return results
    }

    fun getZaakRollen(zaakUrl: URI, roleType: RolType? = null): List<Rol> {
        logger.debug { "Fetching zaak rollen for zaak with URL '$zaakUrl'" }
        return Page.getAll(100) { page ->
            client.getZaakRollen(
                authenticationPluginConfiguration,
                url, zaakUrl, page, roleType
            )
        }
    }

    fun getZaakStatus(zaakUrl: URI): ZaakStatus? {
        logger.debug { "Fetching zaak status for zaak with URL '$zaakUrl'" }
        val zaak = getZaak(zaakUrl)
        return if (zaak.status == null) {
            null
        } else {
            client.getZaakStatus(authenticationPluginConfiguration, URI(zaak.status))
        }
    }

    fun getZaakResultaat(zaakUrl: URI): ZaakResultaat? {
        logger.debug { "Fetching zaak resultaat for zaak with URL '$zaakUrl'" }
        val zaak = getZaak(zaakUrl)
        return if (zaak.resultaat == null) {
            null
        } else {
            client.getZaakResultaat(authenticationPluginConfiguration, zaak.resultaat)
        }
    }

    fun getZaak(zaakUrl: URI): ZaakResponse {
        logger.debug { "Fetching zaak for zaak URL '$zaakUrl'" }
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
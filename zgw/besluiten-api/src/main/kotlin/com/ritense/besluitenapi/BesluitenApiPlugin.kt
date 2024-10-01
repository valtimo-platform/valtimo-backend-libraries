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

package com.ritense.besluitenapi

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.besluitenapi.client.Besluit
import com.ritense.besluitenapi.client.BesluitenApiClient
import com.ritense.besluitenapi.client.CreateBesluitInformatieObject
import com.ritense.besluitenapi.client.CreateBesluitRequest
import com.ritense.besluitenapi.client.Vervalreden
import com.ritense.logging.withLoggingContext
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.validation.Url
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zgw.LoggingConstants.BESLUITEN_API
import com.ritense.zgw.LoggingConstants.DOCUMENTEN_API
import com.ritense.zgw.Rsin
import mu.KLogger
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@Plugin(
    key = BesluitenApiPlugin.PLUGIN_KEY,
    title = "Besluiten API",
    description = "Connects to the Besluiten API"
)
class BesluitenApiPlugin(
    private val besluitenApiClient: BesluitenApiClient,
    private val zaakUrlProvider: ZaakUrlProvider,
) {
    @Url
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "rsin", secret = false)
    lateinit var rsin: Rsin

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: BesluitenApiAuthentication

    @PluginAction(
        key = "link-document-to-besluit",
        title = "Link Document to besluit",
        description = "Links a document to a besluit",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun linkDocumentToBesluit(
        @PluginActionProperty documentUrl: String,
        @PluginActionProperty besluitUrl: String
    ) = linkDocumentToBesluit(URI(documentUrl), URI(besluitUrl))

    fun linkDocumentToBesluit(
        documentUrl: URI,
        besluitUrl: URI
    ) = withLoggingContext(
        DOCUMENTEN_API.ENKELVOUDIG_INFORMATIE_OBJECT to documentUrl.toString(),
        BESLUITEN_API.BESLUIT to besluitUrl.toString()
    ) {
        logger.info { "Linking ZGW document $documentUrl to besluit $besluitUrl" }
        besluitenApiClient.createBesluitInformatieObject(
            authenticationPluginConfiguration,
            url,
            CreateBesluitInformatieObject(documentUrl.toString(), besluitUrl.toString())
        )
    }

    @PluginAction(
        key = "create-besluit",
        title = "Create besluit",
        description = "Creates a besluit in the Besluiten API",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun createBesluit(
        execution: DelegateExecution,
        @PluginActionProperty besluittypeUrl: String,
        @PluginActionProperty toelichting: String?,
        @PluginActionProperty bestuursorgaan: String?,
        @PluginActionProperty ingangsdatum: LocalDate?,
        @PluginActionProperty vervaldatum: LocalDate?,
        @PluginActionProperty vervalreden: Vervalreden?,
        @PluginActionProperty publicatiedatum: LocalDate?,
        @PluginActionProperty verzenddatum: LocalDate?,
        @PluginActionProperty uiterlijkeReactieDatum: LocalDate?,
        @PluginActionProperty createdBesluitUrl: String?,
    ) {
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)
        withLoggingContext(
            "com.ritense.document.domain.impl.JsonSchemaDocument" to documentId.toString()
        ) {
            val besluit = createBesluit(
                zaakUrl = zaakUrl,
                besluittypeUrl = URI(besluittypeUrl),
                ingangsdatum = ingangsdatum ?: LocalDate.now(),
                toelichting = toelichting,
                bestuursorgaan = bestuursorgaan,
                vervaldatum = vervaldatum,
                vervalreden = vervalreden,
                publicatiedatum = publicatiedatum,
                verzenddatum = verzenddatum,
                uiterlijkeReactieDatum = uiterlijkeReactieDatum
            )
            createdBesluitUrl?.let {
                logger.info { "Storing reference to newly created besluit ${besluit.url} in process variable $it" }
                execution.setVariable(it, besluit.url)
            }
        }
    }

    fun createBesluit(
        zaakUrl: URI,
        besluittypeUrl: URI,
        toelichting: String? = null,
        bestuursorgaan: String? = null,
        ingangsdatum: LocalDate? = null,
        vervaldatum: LocalDate? = null,
        vervalreden: Vervalreden? = null,
        publicatiedatum: LocalDate? = null,
        verzenddatum: LocalDate? = null,
        uiterlijkeReactieDatum: LocalDate? = null,
    ): Besluit {
        withLoggingContext("zaakUrl" to zaakUrl.toString()) {
            logger.info { "Creating besluit for zaak $zaakUrl of type $besluittypeUrl" }
            return besluitenApiClient.createBesluit(
                authentication = authenticationPluginConfiguration,
                baseUrl = url,
                request = CreateBesluitRequest(
                    zaak = zaakUrl,
                    besluittype = besluittypeUrl,
                    verantwoordelijkeOrganisatie = rsin.toString(),
                    datum = LocalDate.now(),
                    ingangsdatum = ingangsdatum ?: LocalDate.now(),
                    toelichting = toelichting,
                    bestuursorgaan = bestuursorgaan,
                    vervaldatum = vervaldatum,
                    vervalreden = vervalreden,
                    publicatiedatum = publicatiedatum,
                    verzenddatum = verzenddatum,
                    uiterlijkeReactiedatum = uiterlijkeReactieDatum
                )
            )
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PLUGIN_KEY = "besluitenapi"
        const val URL_PROPERTY = "url"

        fun findConfigurationByUrl(url: URI) = { properties: JsonNode ->
            url.toString().startsWith(properties[URL_PROPERTY].textValue())
        }
    }
}
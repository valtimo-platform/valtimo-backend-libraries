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

package com.ritense.besluitenapi

import com.ritense.besluitenapi.client.BesluitenApiClient
import com.ritense.besluitenapi.client.CreateBesluitRequest
import com.ritense.besluitenapi.client.Vervalreden
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zgw.Rsin
import mu.KLogger
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@Plugin(key = BesluitenApiPlugin.PLUGIN_KEY,
    title = "Besluiten API",
    description = "Connects to the Besluiten API")
class BesluitenApiPlugin(
    private val besluitenApiClient: BesluitenApiClient,
    private val zaakUrlProvider: ZaakUrlProvider,
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "rsin", secret = false)
    lateinit var rsin: Rsin

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: BesluitenApiAuthentication


    @PluginAction(
        key = "create-besluit",
        title = "Create besluit",
        description = "Creates a besluit in the Besluiten API",
        activityTypes = [ActivityType.SERVICE_TASK_START]
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

        logger.debug { "Creating besluit for zaak $zaakUrl of type $besluittypeUrl" }

        val besluit = besluitenApiClient.createBesluit(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            request = CreateBesluitRequest(
                zaak = zaakUrl,
                besluittype = URI(besluittypeUrl),
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

        createdBesluitUrl?.let {
            logger.debug { "Settings resulting variable $it to ${besluit.url}" }
            execution.setVariable(it, besluit.url)
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PLUGIN_KEY = "besluitenapi"
    }
}
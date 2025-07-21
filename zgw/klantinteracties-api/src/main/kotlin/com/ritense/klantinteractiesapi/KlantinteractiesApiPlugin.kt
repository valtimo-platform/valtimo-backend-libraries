/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.klantinteractiesapi

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.klantinteractiesapi.client.KlantinteractiesApiClient
import com.ritense.klantinteractiesapi.client.dto.CreatePartijIdentificatorLink
import com.ritense.klantinteractiesapi.client.dto.CreatePartijRequest
import com.ritense.klantinteractiesapi.client.dto.GetPartijenRequest
import com.ritense.klantinteractiesapi.domain.CodeObjecttype
import com.ritense.klantinteractiesapi.domain.CodeRegister
import com.ritense.klantinteractiesapi.domain.Contactnaam
import com.ritense.klantinteractiesapi.domain.Partij
import com.ritense.klantinteractiesapi.domain.PartijIdentificator
import com.ritense.klantinteractiesapi.domain.PartijSoort
import com.ritense.klantinteractiesapi.domain.PersoonPartijIndentificatie
import com.ritense.klantinteractiesapi.domain.SoortObject
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.validation.Url
import com.ritense.zgw.Page
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.UUID

@Plugin(
    key = KlantinteractiesApiPlugin.PLUGIN_KEY,
    title = "Klantinteracties API",
    description = "Connects to the Klantinteracties API"
)
class KlantinteractiesApiPlugin(
    private val klantinteractiesApiClient: KlantinteractiesApiClient,
) {
    @Url
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: KlantinteractiesApiAuthentication

    @PluginAction(
        key = "create-persoon",
        title = "Create persoon",
        description = "Creates a persoon in the Klantinteracties API",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun createPersoon(
        execution: DelegateExecution,
        @PluginActionProperty bsn: String? = null,
        @PluginActionProperty voorletters: String? = null,
        @PluginActionProperty voornaam: String? = null,
        @PluginActionProperty voorvoegselAchternaam: String? = null,
        @PluginActionProperty achternaam: String? = null,
        @PluginActionProperty processVariableName: String? = null
    ): Partij {
        val partijIdentificatoren = bsn?.let {
            listOf(
                CreatePartijIdentificatorLink(
                    partijIdentificator = PartijIdentificator(
                        codeObjecttype = CodeObjecttype.NATUURLIJK_PERSOON,
                        codeSoortObjectId = SoortObject.BSN,
                        objectId = bsn,
                        codeRegister = CodeRegister.BRP,
                    )
                )
            )
        } ?: emptyList()
        val persoonPartij = klantinteractiesApiClient.createPartij(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            request = CreatePartijRequest(
                soortPartij = PartijSoort.PERSOON,
                indicatieActief = true,
                partijIdentificatoren = partijIdentificatoren,
                partijIdentificatie = PersoonPartijIndentificatie(
                    contactnaam = Contactnaam(
                        voorletters = voorletters ?: "",
                        voornaam = voornaam ?: "",
                        voorvoegselAchternaam = voorvoegselAchternaam ?: "",
                        achternaam = achternaam ?: "",
                    )
                ),
            )
        )

        processVariableName?.let {
            execution.setVariable(processVariableName, persoonPartij.url.toString())
        }

        logger.info { "Created persoon ${persoonPartij.url}" }
        return persoonPartij
    }

    fun getPartij(partijUrl: URI): Partij {
        return klantinteractiesApiClient.getParij(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            partijUrl = partijUrl
        )
    }

    fun getParijUrl(parijId: UUID): URI {
        return UriComponentsBuilder
            .fromUri(url)
            .pathSegment("partijen")
            .pathSegment(parijId.toString())
            .build()
            .toUri()
    }

    fun getPartijen(request: GetPartijenRequest, pageable: Pageable): Page<Partij> {
        return klantinteractiesApiClient.getParijen(
            authentication = authenticationPluginConfiguration,
            baseUrl = url,
            request = request,
            pageable = pageable,
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val PLUGIN_KEY = "klantinteractiesapi"
        const val URL_PROPERTY = "url"

        fun findConfigurationByUrl(url: URI) = { properties: JsonNode ->
            url.toString().startsWith(properties[URL_PROPERTY].textValue())
        }
    }
}
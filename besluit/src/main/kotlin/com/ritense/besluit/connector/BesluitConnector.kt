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

package com.ritense.besluit.connector

import com.ritense.besluit.client.BesluitClient
import com.ritense.besluit.domain.Besluit
import com.ritense.besluit.domain.BesluitInformatieobjectRelatie
import com.ritense.besluit.domain.request.BesluitInformatieobjectRelatieRequest
import com.ritense.besluit.domain.request.CreateBesluitRequest
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.processdocument.event.BesluitAddedEvent
import com.ritense.valtimo.contract.audit.utils.AuditHelper
import com.ritense.valtimo.contract.utils.RequestHelper
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@ConnectorType(name = "Besluiten")
class BesluitConnector(
    private var besluitProperties: BesluitProperties,
    private var besluitClient: BesluitClient,
    private var applicationEventPublisher: ApplicationEventPublisher
) : Connector {

    /**
     * Create a Besluit
     *
     * @param zaakUri - The URI of the zaak
     * @param besluitTypeUri - The URI of the besluittype
     */
    fun createBesluit(zaakUri: URI, besluitTypeUri: URI, businessKey: String): Besluit {
        val request = CreateBesluitRequest(
            verantwoordelijkeOrganisatie = besluitProperties.rsin.toString(),
            besluittype = besluitTypeUri,
            zaak = zaakUri,
            datum = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            ingangsdatum = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

        return runBlocking {
            val besluit = besluitClient.createBesluit(request, getProperties())
            logger.info { "Successfully created besluit ${besluit.identificatie}" }
            publishBesluitAddedEvent(besluit, businessKey)
            return@runBlocking besluit
        }
    }

    fun createBesluitInformatieobjectRelatie(informatieobject: URI, besluit: URI): BesluitInformatieobjectRelatie {
        val request = BesluitInformatieobjectRelatieRequest(
            informatieobject = informatieobject,
            besluit = besluit,
        )

        return runBlocking {
            val result = besluitClient.createBesluitInformatieobjectRelatie(request, getProperties())
            logger.info { "Successfully created relation between besluit and informatieobject ${result.url}" }
            return@runBlocking result
        }
    }

    override fun getProperties(): BesluitProperties {
        return besluitProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        besluitProperties = connectorProperties as BesluitProperties
    }

    private fun publishBesluitAddedEvent(besluit: Besluit, businessKey: String) {
        applicationEventPublisher.publishEvent(
            BesluitAddedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                besluit.identificatie,
                businessKey
            )
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.listener

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.openzaak.exception.UnmappableOpenZaakPropertyException
import com.ritense.openzaak.service.impl.EigenschapService
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.springframework.context.event.EventListener
import java.net.URI

class EigenschappenSubmittedListener(
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val eigenschapService: EigenschapService,
    private val zaakService: ZaakService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val objectMapper: ObjectMapper,
) {

    @EventListener(ExternalDataSubmittedEvent::class)
    fun handle(event: ExternalDataSubmittedEvent) {
        event.data["openzaak"]?.let {
            val mappedEigenschappen: MutableMap<URI, String> = mutableMapOf()
            val zaakTypeLink = zaakTypeLinkService.findBy(event.documentDefinition)
            val zaakInstanceLink = zaakInstanceLinkService.getByDocumentId(event.documentId)
            val eigenschappen = eigenschapService.getEigenschappen(zaakTypeLink.zaakTypeUrl).results
            if (eigenschappen.isNotEmpty()) {
                it.forEach { (key, value) ->
                    eigenschappen.forEach { e ->
                        if (e.naam == key) {
                            mappedEigenschappen[e.url!!] = getStringValue(value)
                        }
                    }
                }
                if (mappedEigenschappen.isNotEmpty()) {
                    zaakTypeLink.assignZaakInstanceEigenschappen(zaakInstanceLink, mappedEigenschappen)
                }
                zaakTypeLinkService.modify(zaakTypeLink)
            } else
                throw UnmappableOpenZaakPropertyException(
                    "Cannot process form variables prefixed with 'openzaak'. Please check Open Zaak for the available 'Eigenschappen'"
                )
        }
    }

    private fun getStringValue(value: Any): String {
        return if (value is JsonNode) {
            objectMapper.treeToValue<String>(value)
        } else {
            value.toString()
        }
    }

}

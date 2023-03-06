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

package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.client.OpenKlantClientProperties
import com.ritense.klant.domain.Klant
import com.ritense.klant.service.KlantService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import java.net.URI
import java.util.UUID

class KlantService(
    private val openKlantClientProperties: OpenKlantClientProperties,
    private val openKlantClient: OpenKlantClient,
    private val zaakRolService: ZaakRolService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService
): KlantService {
    override fun getKlantForDocument(documentId: UUID): Klant {
        val zaakLink = zaakInstanceLinkService.getByDocumentId(documentId)
        val zaakRolPage = zaakRolService.getZaakInitator(zaakLink.zaakInstanceUrl)

        val rollen = zaakRolPage.results
        if (rollen.size != 1) {
            throw IllegalStateException("A single zaak iniator role is needed - found ${rollen.size}")
        }

        val initiator = rollen.first()
        if (initiator.betrokkene == null) {
            throw IllegalStateException("betrokkene is not set in initator role for zaak")
        }
        if (!initiator.betrokkene.toString().startsWith(openKlantClientProperties.url)){
            throw IllegalStateException("betrokkene base url does not match configured klant api base url")
        }

        val klantId = extractId(initiator.betrokkene!!)
        return openKlantClient.getKlant(klantId)
    }

    private fun extractId(uri: URI): UUID {
        return UUID.fromString(uri.path.substringAfterLast("/"))
    }
}

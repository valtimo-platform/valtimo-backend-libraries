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

package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.client.OpenKlantClientProperties
import com.ritense.klant.domain.Klant
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.zaak.BetrokkeneType
import com.ritense.openzaak.service.impl.model.zaak.Rol
import com.ritense.zakenapi.domain.ZaakInstanceLink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.UUID

internal class KlantServiceTest {

    val openKlantClientProperties = mock<OpenKlantClientProperties>()
    val openKlantClient = mock<OpenKlantClient>()
    val zaakRolService = mock<ZaakRolService>()
    val zaakInstanceLinkService = mock<ZaakInstanceLinkService>()

    val klantService = KlantService(openKlantClientProperties, openKlantClient, zaakRolService, zaakInstanceLinkService)

    @Test
    fun `getKlantForDocument gets klant when information is valid`() {
        val documentId = UUID.randomUUID()
        val klantId = UUID.randomUUID()
        val zaakInstanceUrl = URI("http://example.org")
        val zaakLink = mock<ZaakInstanceLink>()

        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakLink)
        whenever(zaakLink.zaakInstanceUrl).thenReturn(zaakInstanceUrl)
        whenever(zaakRolService.getZaakInitator(zaakInstanceUrl)).thenReturn(ResultWrapper(
            count = 1,
            results = listOf(
                Rol(
                    URI("http://zaak.uri"),
                    URI("http://betrokkene.uri/with/id/${klantId}"),
                    BetrokkeneType.NATUURLIJK_PERSOON,
                    URI("http://role.type"),
                    "role description",
                    null
                )
            )
        ))
        whenever(openKlantClientProperties.url).thenReturn("http://betrokkene.uri")
        whenever(openKlantClient.getKlant(klantId)).thenReturn(
            Klant(
                "http://betrokkene.uri/with/id/${klantId}",
                "0612345678",
                "user@example.org"
            )
        )

        val klant = klantService.getKlantForDocument(documentId)

        assertEquals("http://betrokkene.uri/with/id/${klantId}", klant.url)
        assertEquals("user@example.org", klant.emailadres)
        assertEquals("0612345678", klant.telefoonnummer)
    }

    @Test
    fun `getKlantForDocument throws IllegalStateException when getting a unexpected number of initators`() {
        val documentId = UUID.randomUUID()
        val zaakInstanceUrl = URI("http://example.org")
        val zaakLink = mock<ZaakInstanceLink>()

        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakLink)
        whenever(zaakLink.zaakInstanceUrl).thenReturn(zaakInstanceUrl)
        whenever(zaakRolService.getZaakInitator(zaakInstanceUrl)).thenReturn(ResultWrapper(
            count = 0,
            results = emptyList()
        ))

        val exception = assertThrows(IllegalStateException::class.java, {
            klantService.getKlantForDocument(documentId)
        })

        assertEquals("A single zaak iniator role is needed - found 0", exception.message)
    }

    @Test
    fun `getKlantForDocument throws IllegalStateException when betrokkene is not set`() {
        val documentId = UUID.randomUUID()
        val zaakInstanceUrl = URI("http://example.org")
        val zaakLink = mock<ZaakInstanceLink>()

        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakLink)
        whenever(zaakLink.zaakInstanceUrl).thenReturn(zaakInstanceUrl)
        whenever(zaakRolService.getZaakInitator(zaakInstanceUrl)).thenReturn(ResultWrapper(
            count = 1,
            results = listOf(
                Rol(
                    URI("http://zaak.uri"),
                    null,
                    BetrokkeneType.NATUURLIJK_PERSOON,
                    URI("http://role.type"),
                    "role description",
                    null
                )
            )
        ))

        val exception = assertThrows(IllegalStateException::class.java, {
            klantService.getKlantForDocument(documentId)
        })

        assertEquals("betrokkene is not set in initator role for zaak", exception.message)
    }

    @Test
    fun `getKlantForDocument throws IllegalStateException when betrokkene does not match klantapi configuration`() {
        val documentId = UUID.randomUUID()
        val zaakInstanceUrl = URI("http://example.org")
        val zaakLink = mock<ZaakInstanceLink>()

        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakLink)
        whenever(zaakLink.zaakInstanceUrl).thenReturn(zaakInstanceUrl)
        whenever(zaakRolService.getZaakInitator(zaakInstanceUrl)).thenReturn(ResultWrapper(
            count = 1,
            results = listOf(
                Rol(
                    URI("http://zaak.uri"),
                    URI("http://betrokkene.uri"),
                    BetrokkeneType.NATUURLIJK_PERSOON,
                    URI("http://role.type"),
                    "role description",
                    null
                )
            )
        ))
        whenever(openKlantClientProperties.url).thenReturn("http://some-other.url")

        val exception = assertThrows(IllegalStateException::class.java, {
            klantService.getKlantForDocument(documentId)
        })

        assertEquals("betrokkene base url does not match configured klant api base url", exception.message)
    }
}

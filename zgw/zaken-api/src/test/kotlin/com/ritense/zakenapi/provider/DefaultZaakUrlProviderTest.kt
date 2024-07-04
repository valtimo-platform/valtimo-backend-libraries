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

package com.ritense.zakenapi.provider

import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakInstanceLinkId
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.UUID

class DefaultZaakUrlProviderTest {

    lateinit var zaakUrlProvider: DefaultZaakUrlProvider

    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService

    @BeforeEach
    fun setup() {
        zaakInstanceLinkService = mock()
        zaakUrlProvider = DefaultZaakUrlProvider(zaakInstanceLinkService)
    }

    @Test
    fun `should get zaak URL by document id`() {
        val documentId = UUID.randomUUID()

        val zaakInstanceLink = createZaakInstanceLink(documentId)
        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakInstanceLink)
        val zaakUrl = zaakUrlProvider.getZaakUrl(documentId)

        assertThat(zaakUrl).isEqualTo(zaakInstanceLink.zaakInstanceUrl)
    }

    @Test
    fun `should propagate exception from zaakInstanceLinkService`() {
        val documentId = UUID.randomUUID()

        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenThrow(ZaakInstanceLinkNotFoundException("test"))
        val ex = assertThrows<ZaakInstanceLinkNotFoundException> {
            zaakUrlProvider.getZaakUrl(documentId)
        }

        assertThat(ex.message).isEqualTo("test")
    }

    private fun createZaakInstanceLink(documentId: UUID): ZaakInstanceLink {
        val zaakId = UUID.randomUUID()
        return ZaakInstanceLink(
            ZaakInstanceLinkId.newId(UUID.randomUUID()),
            URI("http://localhost/$zaakId"),
            zaakId,
            documentId,
            URI("http://localhost/sometype")
        )
    }
}
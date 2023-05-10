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

package com.ritense.openzaak.plugin

import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakInstanceLinkId
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.UUID

internal class OpenZaakUrlProviderTest {

    val zaakInstanceLinkService = mock<ZaakInstanceLinkService>()
    val zaakTypeLinkService = mock<ZaakTypeLinkService>()
    val openZaakUrlProvider = OpenZaakUrlProvider(zaakInstanceLinkService, zaakTypeLinkService)

    @Test
    fun `should get zaak for document id`() {
        whenever(zaakInstanceLinkService.getByDocumentId(any())).thenReturn(ZaakInstanceLink(
            ZaakInstanceLinkId.newId(UUID.randomUUID()),
            URI("http://some.url"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            URI("http://some.other.url")
        ))

        val documentId = UUID.randomUUID()
        val zaakUrl = openZaakUrlProvider.getZaakUrl(documentId)

        assertEquals("http://some.url", zaakUrl.toString())
        verify(zaakInstanceLinkService).getByDocumentId(documentId)
    }

    @Test
    fun `should get zaak type url for document type`() {
        val documentDefinitionName = "test"
        whenever(zaakTypeLinkService.get(documentDefinitionName)).thenReturn(
            ZaakTypeLink(
                ZaakTypeLinkId.newId(UUID.randomUUID()),
                "test",
                URI("http://some.url"),
                ServiceTaskHandlers(),
                false
            )
        )

        val zaakTypeUrl = openZaakUrlProvider.getZaaktypeUrl(documentDefinitionName)

        assertEquals(URI("http://some.url"), zaakTypeUrl)
        verify(zaakTypeLinkService).get(documentDefinitionName)
    }
}

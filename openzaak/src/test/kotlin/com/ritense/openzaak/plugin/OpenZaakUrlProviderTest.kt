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

package com.ritense.openzaak.plugin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinkId
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.net.URI
import java.util.UUID

internal class OpenZaakUrlProviderTest {

    val zaakInstanceLinkService = mock<ZaakInstanceLinkService>()
    val openZaakUrlProvider = OpenZaakUrlProvider(zaakInstanceLinkService)

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
        val zaakUrl = openZaakUrlProvider.getZaak(documentId)

        assertEquals("http://some.url", zaakUrl)
        verify(zaakInstanceLinkService).getByDocumentId(documentId)
    }
}
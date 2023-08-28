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

import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.service.impl.EigenschapService
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.EigenschapType
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.UUID

internal class EigenschappenSubmittedListenerTest {

    @InjectMocks
    lateinit var eigenschappenSubmittedListener: EigenschappenSubmittedListener

    @Mock
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @Mock
    lateinit var eigenschapService: EigenschapService

    @Mock
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService

    @Mock
    lateinit var zaakInstanceLink: ZaakInstanceLink

    // This mock is not used in the code but required for the MockInject of eigenschappenSubmittedListener
    @Mock
    lateinit var zaakService: ZaakService

    lateinit var event: ExternalDataSubmittedEvent

    @Mock
    lateinit var zaakTypeLink: ZaakTypeLink

    val documentDefinition = "documentDefinition"
    val documentId = UUID.randomUUID()
    val zaakTypeUrl = URI.create("zaakTypeUrl")
    val uriVoornaam = URI.create("uriVoornaam")

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        event = ExternalDataSubmittedEvent(
            mapOf("OpenZaak".lowercase() to mapOf("voornaam" to "Piet")),
            documentDefinition,
            documentId
        )
    }

    @Test
    fun `handle incoming eigenschappen submitted event`() {
        whenever(zaakTypeLinkService.findBy(eq(documentDefinition))).thenReturn(zaakTypeLink)

        whenever(zaakTypeLink.zaakTypeUrl).thenReturn(zaakTypeUrl)
        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakInstanceLink)

        whenever(eigenschapService.getEigenschappen(eq(zaakTypeUrl))).thenReturn(
            ResultWrapper(
                1,
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                listOf(
                    EigenschapType(
                        uriVoornaam,
                        "voornaam",
                        "voornaam",
                        zaakTypeUrl
                    )
                )
            )
        )
        eigenschappenSubmittedListener.handle(event)

        verify(zaakTypeLink).assignZaakInstanceEigenschappen(eq(zaakInstanceLink), eq(mutableMapOf(uriVoornaam to "Piet")))
    }

}

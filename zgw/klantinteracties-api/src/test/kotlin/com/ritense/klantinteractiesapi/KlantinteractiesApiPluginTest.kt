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

import com.ritense.klantinteractiesapi.client.KlantinteractiesApiClient
import com.ritense.klantinteractiesapi.client.dto.CreatePartijRequest
import com.ritense.klantinteractiesapi.domain.Partij
import com.ritense.klantinteractiesapi.domain.PartijSoort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution
import java.net.URI
import java.util.UUID

class KlantinteractiesApiPluginTest {

    lateinit var klantinteractiesApiPlugin: KlantinteractiesApiPlugin
    lateinit var klantinteractiesApiClient: KlantinteractiesApiClient


    @BeforeEach
    fun init() {
        klantinteractiesApiClient = mock()
        klantinteractiesApiPlugin = KlantinteractiesApiPlugin(klantinteractiesApiClient)
        klantinteractiesApiPlugin.authenticationPluginConfiguration = mock()
        klantinteractiesApiPlugin.url =
            URI.create("https://some-host.nl/klantinteracties/api/v1/klantinformatieobjecten")
    }

    @Test
    fun `should call client`() {
        val authenticationMock = mock<KlantinteractiesApiAuthentication>()
        val executionMock = mock<DelegateExecution>()

        val plugin = KlantinteractiesApiPlugin(klantinteractiesApiClient)
        plugin.url = URI("http://klantinteracties.api")
        plugin.authenticationPluginConfiguration = authenticationMock

        val authenticationCaptor = argumentCaptor<KlantinteractiesApiAuthentication>()
        val uriCaptor = argumentCaptor<URI>()
        val requestCaptor = argumentCaptor<CreatePartijRequest>()
        val partij = Partij(
            uuid = UUID.randomUUID(),
            url = URI("http://openklant.api/partij/000001"),
            partijIdentificatoren = emptyList(),
            soortPartij = PartijSoort.PERSOON,
            indicatieActief = true,
            partijIdentificatie = mock(),
        )
        whenever(
            klantinteractiesApiClient.createPartij(
                authenticationCaptor.capture(),
                uriCaptor.capture(),
                requestCaptor.capture()
            )
        ).thenReturn(partij)

        plugin.createPersoon(
            execution = executionMock,
            bsn = "000000000",
            voorletters = "J",
            voornaam = "John",
            voorvoegselAchternaam = null,
            achternaam = "Doe",
            processVariableName = "parijUrl",
        )

        verify(executionMock).setVariable("parijUrl", "http://openklant.api/partij/000001")
        assertEquals(URI("http://klantinteracties.api"), uriCaptor.firstValue)
        assertEquals(authenticationMock, authenticationCaptor.firstValue)
        val createKlantRequest = requestCaptor.firstValue
        assertEquals(PartijSoort.PERSOON, createKlantRequest.soortPartij)
        assertTrue(createKlantRequest.indicatieActief)
    }
}
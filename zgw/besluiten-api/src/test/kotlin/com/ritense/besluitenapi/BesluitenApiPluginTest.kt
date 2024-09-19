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

package com.ritense.besluitenapi

import com.ritense.besluitenapi.client.Besluit
import com.ritense.besluitenapi.client.BesluitInformatieObject
import com.ritense.besluitenapi.client.BesluitenApiClient
import com.ritense.besluitenapi.client.CreateBesluitInformatieObject
import com.ritense.besluitenapi.client.CreateBesluitRequest
import com.ritense.besluitenapi.client.Vervalreden
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zgw.Rsin
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import java.time.LocalDate
import java.util.UUID

class BesluitenApiPluginTest {

    lateinit var besluitenApiPlugin: BesluitenApiPlugin
    lateinit var besluitenApiClient: BesluitenApiClient
    lateinit var zaakUrlProvider: ZaakUrlProvider


    @BeforeEach
    fun init() {
        zaakUrlProvider = mock()
        besluitenApiClient = mock()
        besluitenApiPlugin = BesluitenApiPlugin(besluitenApiClient, zaakUrlProvider)
        besluitenApiPlugin.authenticationPluginConfiguration = mock()
        besluitenApiPlugin.url = URI.create("https://some-host.nl/besluiten/api/v1/besluitinformatieobjecten")
        besluitenApiPlugin.rsin = Rsin("252170362")
    }

    @Test
    fun `should call client when given minimal arguments`() {
        val authenticationMock = mock<BesluitenApiAuthentication>()
        val executionMock = mock<DelegateExecution>()

        val plugin = BesluitenApiPlugin(besluitenApiClient, zaakUrlProvider)
        plugin.url = URI("http://besluiten.api")
        plugin.rsin = Rsin("633182801")
        plugin.authenticationPluginConfiguration = authenticationMock

        val documentId = "c5e1c33f-dbe1-4f76-b93f-2fa6e20e2190"
        val zaakUrl = URI("http://zaak.api/zaak")
        whenever(executionMock.businessKey).thenReturn(documentId)
        whenever(zaakUrlProvider.getZaakUrl(UUID.fromString(documentId))).thenReturn(zaakUrl)

        val authenticationCaptor = argumentCaptor<BesluitenApiAuthentication>()
        val uriCaptor = argumentCaptor<URI>()
        val requestCaptor = argumentCaptor<CreateBesluitRequest>()
        val besluit = mock<Besluit>()
        whenever(
            besluitenApiClient.createBesluit(
                authenticationCaptor.capture(),
                uriCaptor.capture(),
                requestCaptor.capture()
            )
        ).thenReturn(besluit)

        plugin.createBesluit(
            executionMock,
            "http://catalogus.api/besluit",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        // if createdBesluitUrl is null variable should not be set.
        verify(executionMock, never()).setVariable(any(), any())

        // verify creation request
        assertEquals(URI("http://besluiten.api"), uriCaptor.firstValue)
        assertEquals(authenticationMock, authenticationCaptor.firstValue)

        val createBesluitRequest = requestCaptor.firstValue

        assertEquals(URI("http://zaak.api/zaak"), createBesluitRequest.zaak)
        assertEquals(URI("http://catalogus.api/besluit"), createBesluitRequest.besluittype)
        assertEquals("633182801", createBesluitRequest.verantwoordelijkeOrganisatie)
        assertEquals(LocalDate.now(), createBesluitRequest.datum)
        assertEquals(LocalDate.now(), createBesluitRequest.ingangsdatum)
        assertNull(createBesluitRequest.toelichting)
        assertNull(createBesluitRequest.bestuursorgaan)
        assertNull(createBesluitRequest.vervaldatum)
        assertNull(createBesluitRequest.vervalreden)
        assertNull(createBesluitRequest.publicatiedatum)
        assertNull(createBesluitRequest.verzenddatum)
        assertNull(createBesluitRequest.uiterlijkeReactiedatum)
    }

    @Test
    fun `should call client when given all arguments`() {
        val authenticationMock = mock<BesluitenApiAuthentication>()
        val executionMock = mock<DelegateExecution>()

        val plugin = BesluitenApiPlugin(besluitenApiClient, zaakUrlProvider)
        plugin.url = URI("http://besluiten.api")
        plugin.rsin = Rsin("633182801")
        plugin.authenticationPluginConfiguration = authenticationMock

        val documentId = "c5e1c33f-dbe1-4f76-b93f-2fa6e20e2190"
        val zaakUrl = URI("http://zaak.api/zaak")
        val besluitUrl = URI("http://besluiten.api/besluit")
        whenever(executionMock.businessKey).thenReturn(documentId)
        whenever(zaakUrlProvider.getZaakUrl(UUID.fromString(documentId))).thenReturn(zaakUrl)

        val authenticationCaptor = argumentCaptor<BesluitenApiAuthentication>()
        val uriCaptor = argumentCaptor<URI>()
        val requestCaptor = argumentCaptor<CreateBesluitRequest>()
        val besluit = mock<Besluit>()
        whenever(besluit.url).thenReturn(besluitUrl)
        whenever(
            besluitenApiClient.createBesluit(
                authenticationCaptor.capture(),
                uriCaptor.capture(),
                requestCaptor.capture()
            )
        ).thenReturn(besluit)

        plugin.createBesluit(
            executionMock,
            "http://catalogus.api/besluit",
            "toelichting",
            "680572442",
            LocalDate.of(2020, 2, 20),
            LocalDate.of(2020, 2, 21),
            Vervalreden.TIJDELIJK,
            LocalDate.of(2020, 2, 22),
            LocalDate.of(2020, 2, 23),
            LocalDate.of(2020, 2, 24),
            "processVariableName"
        )

        // if createdBesluitUrl is not null variable should be set.
        verify(executionMock).setVariable("processVariableName", besluitUrl)

        // verify creation request
        assertEquals(URI("http://besluiten.api"), uriCaptor.firstValue)
        assertEquals(authenticationMock, authenticationCaptor.firstValue)

        val createBesluitRequest = requestCaptor.firstValue

        assertEquals(URI("http://zaak.api/zaak"), createBesluitRequest.zaak)
        assertEquals(URI("http://catalogus.api/besluit"), createBesluitRequest.besluittype)
        assertEquals("633182801", createBesluitRequest.verantwoordelijkeOrganisatie)
        assertEquals(LocalDate.now(), createBesluitRequest.datum)
        assertEquals(LocalDate.of(2020, 2, 20), createBesluitRequest.ingangsdatum)
        assertEquals("toelichting", createBesluitRequest.toelichting)
        assertEquals("680572442", createBesluitRequest.bestuursorgaan)
        assertEquals(LocalDate.of(2020, 2, 21), createBesluitRequest.vervaldatum)
        assertEquals(Vervalreden.TIJDELIJK, createBesluitRequest.vervalreden)
        assertEquals(LocalDate.of(2020, 2, 22), createBesluitRequest.publicatiedatum)
        assertEquals(LocalDate.of(2020, 2, 23), createBesluitRequest.verzenddatum)
        assertEquals(LocalDate.of(2020, 2, 24), createBesluitRequest.uiterlijkeReactiedatum)
    }

    @Test
    fun `should link document to besluit`() {
        val besluitenApiAuthenticationCaptor = argumentCaptor<BesluitenApiAuthentication>()
        val uriCaptor = argumentCaptor<URI>()
        val besluitInformatieObjectCaptor = argumentCaptor<CreateBesluitInformatieObject>()
        val documentUrl = "https://some-host.nl/documenten/api/v1/${UUID.randomUUID()}"
        val besluitUrl = "https://some-host.nl/besluit/api/v1/besluitobjecten/${UUID.randomUUID()}"
        val besluitInformatieObjectUrl =
            "https://some-host.nl/besluiten/api/v1/besluiteninformatieobjecten/${UUID.randomUUID()}"
        whenever(besluitenApiClient.createBesluitInformatieObject(any(), any(), any())).thenReturn(
            BesluitInformatieObject(besluitInformatieObjectUrl, documentUrl, besluitUrl)
        )
        besluitenApiPlugin.linkDocumentToBesluit(
            documentUrl,
            besluitUrl
        )

        verify(besluitenApiClient).createBesluitInformatieObject(
            besluitenApiAuthenticationCaptor.capture(),
            uriCaptor.capture(),
            besluitInformatieObjectCaptor.capture()
        )
        val besluitInformatieObjectValue = besluitInformatieObjectCaptor.firstValue
        val besluitenApiAuthenticationValue = besluitenApiAuthenticationCaptor.firstValue
        val uriValue = uriCaptor.firstValue

        assertEquals(besluitInformatieObjectValue.besluit, besluitUrl)
        assertEquals(besluitInformatieObjectValue.informatieobject, documentUrl)
        assertEquals(besluitenApiAuthenticationValue, besluitenApiPlugin.authenticationPluginConfiguration)
        assertEquals(uriValue, besluitenApiPlugin.url)
    }
}
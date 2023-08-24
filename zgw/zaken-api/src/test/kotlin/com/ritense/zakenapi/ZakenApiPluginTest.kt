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

package com.ritense.zakenapi

import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.ZakenApiPlugin.Companion.DOCUMENT_URL_PROCESS_VAR
import com.ritense.zakenapi.ZakenApiPlugin.Companion.RESOURCE_ID_PROCESS_VAR
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.domain.*
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zgw.Page
import com.ritense.zgw.Rsin
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ZakenApiPluginTest {

    @Test
    fun `should link document to zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(URI("https://zaak.url"))

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.linkDocumentToZaak(executionMock, "https://document.url", "titel", "beschrijving")

        val captor = argumentCaptor<LinkDocumentRequest>()
        verify(zakenApiClient).linkDocument(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals("https://document.url", request.informatieobject)
        assertEquals("https://zaak.url", request.zaak)
        assertEquals("titel", request.titel)
        assertEquals("beschrijving", request.beschrijving)
    }

    @Test
    fun `should link uploaded document to zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(executionMock.getVariable(DOCUMENT_URL_PROCESS_VAR)).thenReturn("https://document.url")
        whenever(executionMock.getVariable(RESOURCE_ID_PROCESS_VAR)).thenReturn("myResourceId")
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(URI("https://zaak.url"))
        whenever(storageService.getResourceMetadata("myResourceId")).thenReturn(
            mapOf(
                "title" to "titel",
                "description" to "beschrijving",
            )
        )

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.linkUploadedDocumentToZaak(executionMock)

        val captor = argumentCaptor<LinkDocumentRequest>()
        verify(zakenApiClient).linkDocument(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals("https://document.url", request.informatieobject)
        assertEquals("https://zaak.url", request.zaak)
        assertEquals("titel", request.titel)
        assertEquals("beschrijving", request.beschrijving)
    }

    @Test
    fun `should return list of zaakobjecten`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val resultPage = Page(
            2,
            null,
            null,
            listOf<ZaakObject>(
                mock(),
                mock()
            )
        )

        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                URI("https://zaken.plugin.url"),
                URI("https://example.org"),
                1
            )
        ).thenReturn(resultPage)

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        val zaakUrl = URI("https://example.org")
        val zaakObjecten = plugin.getZaakObjecten(zaakUrl)

        assertEquals(2, zaakObjecten.size)
    }

    @Test
    fun `should return full list of zaakobjecten when multiple pages are found`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val firstResultPage = Page(
            2,
            URI("https://zaken.plugin.url/zaken/api/v1/zaakobjecten?page=2"),
            null,
            listOf<ZaakObject>(
                mock(),
                mock()
            )
        )
        val secondResultPage = Page(
            1,
            null,
            URI("https://zaken.plugin.url/zaken/api/v1/zaakobjecten?page=1"),
            listOf<ZaakObject>(
                mock()
            )
        )

        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                URI("https://zaken.plugin.url"),
                URI("https://example.org"),
                1
            )
        ).thenReturn(firstResultPage)
        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                URI("https://zaken.plugin.url"),
                URI("https://example.org"),
                2
            )
        ).thenReturn(secondResultPage)

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        val zaakUrl = URI("https://example.org")
        val zaakObjecten = plugin.getZaakObjecten(zaakUrl)

        verify(zakenApiClient, times(2)).getZaakObjecten(any(), any(), any(), any())
        assertEquals(3, zaakObjecten.size)
    }

    @Test
    fun `should return full list of zaakrollen when multiple pages are found`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val firstResultPage = Page(
            2,
            URI("https://zaken.plugin.url/zaken/api/v1/rollen?page=2"),
            null,
            listOf<Rol>(
                mock(),
                mock()
            )
        )
        val secondResultPage = Page(
            1,
            null,
            URI("https://zaken.plugin.url/zaken/api/v1/rollen?page=1"),
            listOf<Rol>(
                mock()
            )
        )

        whenever(
            zakenApiClient.getZaakRollen(
                authenticationMock,
                URI("https://zaken.plugin.url"),
                URI("https://example.org"),
                1
            )
        ).thenReturn(firstResultPage)
        whenever(
            zakenApiClient.getZaakRollen(
                authenticationMock,
                URI("https://zaken.plugin.url"),
                URI("https://example.org"),
                2
            )
        ).thenReturn(secondResultPage)

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        val zaakUrl = URI("https://example.org")
        val zaakRollen = plugin.getZaakRollen(zaakUrl)

        verify(zakenApiClient, times(2)).getZaakRollen(any(), any(), any(), any(), eq(null))
        assertEquals(3, zaakRollen.size)
    }

    @Test
    fun `should create zaakrol for natuurlijk persoon`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()
        val executionMock = mock<DelegateExecution>()

        val documentId = UUID.randomUUID()
        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(URI("https://zaak.uri"))

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.createNatuurlijkPersoonZaakRol(
            executionMock,
            "http://roltype.uri",
            "rolToelichting",
            "inpBsn",
            "anpIdentificatie",
            "inpA_nummer"
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue

        assertEquals(URI("https://zaak.uri"), rol.zaak)
        assertEquals(URI("http://roltype.uri"), rol.roltype)
        assertEquals("rolToelichting", rol.roltoelichting)

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolNatuurlijkPersoon
        assertEquals("inpBsn", betrokkeneIdentificatie.inpBsn)
        assertEquals("anpIdentificatie", betrokkeneIdentificatie.anpIdentificatie)
        assertEquals("inpA_nummer", betrokkeneIdentificatie.inpA_nummer)
    }

    @Test
    fun `should create zaakrol for niet-natuurlijk persoon`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()
        val executionMock = mock<DelegateExecution>()

        val documentId = UUID.randomUUID()
        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(URI("https://zaak.uri"))

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.createNietNatuurlijkPersoonZaakRol(
            executionMock,
            "http://roltype.uri",
            "rolToelichting",
            "innNnpId",
            "annIdentificatie"
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue

        assertEquals(URI("https://zaak.uri"), rol.zaak)
        assertEquals(URI("http://roltype.uri"), rol.roltype)
        assertEquals("rolToelichting", rol.roltoelichting)

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolNietNatuurlijkPersoon
        assertEquals("annIdentificatie", betrokkeneIdentificatie.annIdentificatie)
        assertEquals("innNnpId", betrokkeneIdentificatie.innNnpId)
    }


    @Test
    fun `should create zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val rsin = Rsin("051845623")
        val zaaktypeUrl = URI("https://example.com/zaaktype/1234")

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(
            zakenApiClient.createZaak(
                eq(authenticationMock),
                eq(URI("https://zaken.plugin.url")),
                any()
            )
        ).thenReturn(
            CreateZaakResponse(
                url = URI("https://zaken.plugin.url/1234"),
                uuid = UUID.randomUUID(),
                zaaktype = zaaktypeUrl,
                bronorganisatie = rsin,
                startdatum = LocalDate.now(),
                verantwoordelijkeOrganisatie = rsin,
            )
        )

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.createZaak(executionMock, rsin, zaaktypeUrl)

        val captor = argumentCaptor<CreateZaakRequest>()
        verify(zakenApiClient).createZaak(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(rsin, request.bronorganisatie)
        assertEquals(zaaktypeUrl, request.zaaktype)
        assertEquals(rsin, request.verantwoordelijkeOrganisatie)
        assertNotNull(request.startdatum)
    }


    @Test
    fun `should create zaak status`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = URI("https://example.com/zaken/1234")
        val statustypeUrl = URI("https://example.com/statustypen/1234")

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.setZaakStatus(executionMock, statustypeUrl, "Status description")

        val captor = argumentCaptor<CreateZaakStatusRequest>()
        verify(zakenApiClient).createZaakStatus(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(zaakUrl, request.zaak)
        assertEquals(statustypeUrl, request.statustype)
        assertNotNull(request.datumStatusGezet)
        assertEquals("Status description", request.statustoelichting)
    }


    @Test
    fun `should create zaak resultaat`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = URI("https://example.com/zaken/1234")
        val resultaattypeUrl = URI("https://example.com/resultaten/1234")

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.createZaakResultaat(executionMock, resultaattypeUrl, "Result description")

        val captor = argumentCaptor<CreateZaakResultaatRequest>()
        verify(zakenApiClient).createZaakResultaat(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(zaakUrl, request.zaak)
        assertEquals(resultaattypeUrl, request.resultaattype)
        assertEquals("Result description", request.toelichting)
    }

    @Test
    fun `should update zaakopschorting and verlenging`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = URI("https://example.com/zaken/1234")

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        // when
        plugin.setZaakOpschorting(
            execution = executionMock,
            verlengingsduur = "P3Y",
            toelichtingVerlenging = "testing verlenging",
            toelichtingOpschorting = "testing opschorting"
        )

        // then
        val captor = argumentCaptor<SetZaakopschortingRequest>()
        verify(zakenApiClient).setZaakOpschorting(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals("true", request.opschorting.indicatie)
        assertEquals("testing verlenging", request.verlenging.reden)
        assertEquals("testing opschorting", request.opschorting.reden)
    }
    @Test
    fun `should set zaakopschorting to false`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = URI("https://example.com/zaken/1234")

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val plugin = ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository
        )
        plugin.url = URI("https://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        // when
        plugin.continueZaakAfterOpschorting(execution = executionMock)

        // then
        verify(zakenApiClient, times(1)).continueZaakAfterOpschorting(any(), any())
    }
}

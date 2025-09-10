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

package com.ritense.zakenapi

import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.zakenapi.ZakenApiPlugin.Companion.DOCUMENT_URL_PROCESS_VAR
import com.ritense.zakenapi.ZakenApiPlugin.Companion.RESOURCE_ID_PROCESS_VAR
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.domain.AardRelatie
import com.ritense.zakenapi.domain.Betalingsindicatie
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.CreateZaakResultaatRequest
import com.ritense.zakenapi.domain.CreateZaakStatusRequest
import com.ritense.zakenapi.domain.CreateZaakeigenschapRequest
import com.ritense.zakenapi.domain.Geometry
import com.ritense.zakenapi.domain.GeometryType
import com.ritense.zakenapi.domain.PatchZaakRequest
import com.ritense.zakenapi.domain.UpdateZaakeigenschapRequest
import com.ritense.zakenapi.domain.ZaakHersteltermijn
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.domain.ZaakObjectRequest
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.domain.ZaakeigenschapResponse
import com.ritense.zakenapi.domain.ZaakopschortingRequest
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolMedewerker
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolOrganisatorischeEenheid
import com.ritense.zakenapi.domain.rol.RolVestiging
import com.ritense.zakenapi.repository.ZaakHersteltermijnRepository
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zgw.Page
import com.ritense.zgw.Rsin
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.transaction.PlatformTransactionManager
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ZakenApiPluginTest {

    @Test
    fun `should link document to zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(zaakUri())

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        plugin.linkDocumentToZaak(executionMock, documentUrl(), "titel", "beschrijving")

        val captor = argumentCaptor<LinkDocumentRequest>()
        verify(zakenApiClient).linkDocument(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(documentUrl(), request.informatieobject)
        assertEquals(zaakUrl(), request.zaak)
        assertEquals("titel", request.titel)
        assertEquals("beschrijving", request.beschrijving)
    }

    @Test
    fun `should link uploaded document to zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()
        val documentId = UUID.randomUUID()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(executionMock.getVariable(DOCUMENT_URL_PROCESS_VAR)).thenReturn(documentUrl())
        whenever(executionMock.getVariable(RESOURCE_ID_PROCESS_VAR)).thenReturn("myResourceId")
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(zaakUri())
        whenever(zakenApiClient.linkDocument(any(), any(), any())).thenReturn(mock())
        whenever(storageService.getResourceMetadata("myResourceId")).thenReturn(
            mapOf(
                "title" to "titel",
                "description" to "beschrijving",
            )
        )

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            storageService = storageService,
            authenticationMock = authenticationMock
        )

        plugin.linkUploadedDocumentToZaak(executionMock)

        val captor = argumentCaptor<LinkDocumentRequest>()
        verify(zakenApiClient).linkDocument(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(documentUrl(), request.informatieobject)
        assertEquals(zaakUrl(), request.zaak)
        assertEquals("titel", request.titel)
        assertEquals("beschrijving", request.beschrijving)
    }

    @Test
    fun `should return list of zaakobjecten`() {
        val zakenApiClient: ZakenApiClient = mock()
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
                zakenApiUri(),
                zaakUri(),
                1
            )
        ).thenReturn(resultPage)

        val plugin = zakenApiPlugin(
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        val zaakUrl = zaakUri()
        val zaakObjecten = plugin.getZaakObjecten(zaakUrl)

        assertEquals(2, zaakObjecten.size)
    }

    @Test
    fun `should return full list of zaakobjecten when multiple pages are found`() {
        val zakenApiClient: ZakenApiClient = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()
        val zaakobjectenUrl = "${zakenApiUrl()}/zaken/api/v1/zaakobjecten"

        val firstResultPage = Page(
            2,
            URI("${zaakobjectenUrl}?page=2"),
            null,
            listOf<ZaakObject>(
                mock(),
                mock()
            )
        )
        val secondResultPage = Page(
            1,
            null,
            URI("${zaakobjectenUrl}?page=1"),
            listOf<ZaakObject>(
                mock()
            )
        )

        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                zakenApiUri(),
                zaakUri(),
                1
            )
        ).thenReturn(firstResultPage)
        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                zakenApiUri(),
                zaakUri(),
                2
            )
        ).thenReturn(secondResultPage)

        val plugin = zakenApiPlugin(
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        val zaakUrl = zaakUri()
        val zaakObjecten = plugin.getZaakObjecten(zaakUrl)

        verify(zakenApiClient, times(2)).getZaakObjecten(any(), any(), any(), any())
        assertEquals(3, zaakObjecten.size)
    }

    @Test
    fun `should return full list of zaakrollen when multiple pages are found`() {
        val zakenApiClient: ZakenApiClient = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val firstResultPage = Page(
            2,
            URI("${zakenApiUrl()}/zaken/api/v1/rollen?page=2"),
            null,
            listOf<Rol>(
                mock(),
                mock()
            )
        )
        val secondResultPage = Page(
            1,
            null,
            URI("${zakenApiUrl()}/zaken/api/v1/rollen?page=1"),
            listOf<Rol>(
                mock()
            )
        )

        whenever(
            zakenApiClient.getZaakRollen(
                authenticationMock,
                zakenApiUri(),
                zaakUri(),
                1
            )
        ).thenReturn(firstResultPage)
        whenever(
            zakenApiClient.getZaakRollen(
                authenticationMock,
                zakenApiUri(),
                zaakUri(),
                2
            )
        ).thenReturn(secondResultPage)

        val plugin = zakenApiPlugin(
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        val zaakUrl = zaakUri()
        val zaakRollen = plugin.getZaakRollen(zaakUrl)

        verify(zakenApiClient, times(2)).getZaakRollen(any(), any(), any(), any(), eq(null))
        assertEquals(3, zaakRollen.size)
    }

    @Test
    fun `should create zaakrol for natuurlijk persoon`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()

        val plugin = zakenApiPluginAndMocksForZaakRol(
            zakenApiClient = zakenApiClient,
            executionMock = executionMock
        )

        plugin.createNatuurlijkPersoonZaakRol(
            execution = executionMock,
            roltypeUrl = roltypeUrl(),
            rolToelichting = "rolToelichting",
            inpBsn = "inpBsn",
            anpIdentificatie = "anpIdentificatie",
            inpA_nummer = "inpA_nummer"
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue

        assertEquals(zaakUri(), rol.zaak)
        assertEquals(roltypeUri(), rol.roltype)
        assertEquals("rolToelichting", rol.roltoelichting)

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolNatuurlijkPersoon
        assertEquals("inpBsn", betrokkeneIdentificatie.inpBsn)
        assertEquals("anpIdentificatie", betrokkeneIdentificatie.anpIdentificatie)
        assertEquals("inpA_nummer", betrokkeneIdentificatie.inpA_nummer)
    }

    @Test
    fun `should create zaakrol for niet-natuurlijk persoon`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()

        val plugin = zakenApiPluginAndMocksForZaakRol(
            zakenApiClient = zakenApiClient,
            executionMock = executionMock
        )

        plugin.createNietNatuurlijkPersoonZaakRol(
            execution = executionMock,
            roltypeUrl = roltypeUrl(),
            rolToelichting = "rolToelichting",
            innNnpId = "innNnpId",
            annIdentificatie = "annIdentificatie",
            kvkNummer = "kvkNummer",
            vestigingsNummer = "vestigingsNummer"
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue

        assertEquals(zaakUri(), rol.zaak)
        assertEquals(roltypeUri(), rol.roltype)
        assertEquals("rolToelichting", rol.roltoelichting)

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolNietNatuurlijkPersoon
        assertThat(betrokkeneIdentificatie.annIdentificatie).isEqualTo("annIdentificatie")
        assertThat(betrokkeneIdentificatie.innNnpId).isEqualTo("innNnpId")
        assertThat(betrokkeneIdentificatie.kvkNummer).isNotNull
        assertThat(betrokkeneIdentificatie.kvkNummer).isEqualTo("kvkNummer")
        assertThat(betrokkeneIdentificatie.vestigingsNummer).isNotNull
        assertThat(betrokkeneIdentificatie.vestigingsNummer).isEqualTo("vestigingsNummer")
    }

    @Test
    fun `should create zaakrol for medewerker without voorvoegselAchternaam`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()

        val plugin = zakenApiPluginAndMocksForZaakRol(
            zakenApiClient = zakenApiClient,
            executionMock = executionMock
        )

        plugin.createMedewerkerZaakRol(
            execution = executionMock,
            roltypeUrl = roltypeUrl(),
            rolToelichting = "rolToelichting",
            identificatie = "identificatie",
            achternaam = "achternaam",
            voorletters = "voorletters",
            indicatieMachtiging = "gemachtigde"
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue

        assertThat(rol.zaak).isEqualTo(zaakUri())
        assertThat(rol.roltype).isEqualTo(roltypeUri())
        assertThat(rol.roltoelichting).isEqualTo("rolToelichting")
        assertThat(rol.indicatieMachtiging).isNotNull
        assertThat(rol.indicatieMachtiging!!.key).isEqualTo("gemachtigde")

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolMedewerker
        assertThat(betrokkeneIdentificatie.identificatie).isEqualTo("identificatie")
        assertThat(betrokkeneIdentificatie.achternaam).isEqualTo("achternaam")
        assertThat(betrokkeneIdentificatie.voorletters).isEqualTo("voorletters")
        assertThat(betrokkeneIdentificatie.voorvoegselAchternaam).isEmpty()
    }

    @Test
    fun `should create zaakrol for medewerker with voorvoegselAchternaam`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()

        val plugin = zakenApiPluginAndMocksForZaakRol(
            zakenApiClient = zakenApiClient,
            executionMock = executionMock
        )

        plugin.createMedewerkerZaakRol(
            execution = executionMock,
            roltypeUrl = roltypeUrl(),
            rolToelichting = "rolToelichting",
            identificatie = "identificatie",
            achternaam = "achternaam",
            voorletters = "voorletters",
            voorvoegselAchternaam = "voorvoegselAchternaam",
            indicatieMachtiging = "gemachtigde"
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue

        assertThat(rol.zaak).isEqualTo(zaakUri())
        assertThat(rol.roltype).isEqualTo(roltypeUri())
        assertThat(rol.roltoelichting).isEqualTo("rolToelichting")
        assertThat(rol.indicatieMachtiging).isNotNull
        assertThat(rol.indicatieMachtiging!!.key).isEqualTo("gemachtigde")

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolMedewerker
        assertThat(betrokkeneIdentificatie.identificatie).isEqualTo("identificatie")
        assertThat(betrokkeneIdentificatie.achternaam).isEqualTo("achternaam")
        assertThat(betrokkeneIdentificatie.voorletters).isEqualTo("voorletters")
        assertThat(betrokkeneIdentificatie.voorvoegselAchternaam).isEqualTo("voorvoegselAchternaam")
    }

    @Test
    fun `should create zaakrol for organisatorische eenheid`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()

        val plugin = zakenApiPluginAndMocksForZaakRol(
            zakenApiClient = zakenApiClient,
            executionMock = executionMock
        )

        plugin.createOrganisatorischeEenheidZaakRol(
            execution = executionMock,
            roltypeUrl = roltypeUrl(),
            rolToelichting = "rolToelichting",
            identificatie = "identificatie",
            naam = "naam",
            isGehuisvestIn = "isGehuisvestIn",
            indicatieMachtiging = "gemachtigde"
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue
        assertThat(rol.zaak).isEqualTo(zaakUri())
        assertThat(rol.roltype).isEqualTo(roltypeUri())
        assertThat(rol.roltoelichting).isEqualTo("rolToelichting")
        assertThat(rol.indicatieMachtiging).isNotNull
        assertThat(rol.indicatieMachtiging!!.key).isEqualTo("gemachtigde")

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolOrganisatorischeEenheid
        assertThat(betrokkeneIdentificatie.identificatie).isEqualTo("identificatie")
        assertThat(betrokkeneIdentificatie.naam).isEqualTo("naam")
        assertThat(betrokkeneIdentificatie.isGehuisvestIn).isEqualTo("isGehuisvestIn")
    }

    @Test
    fun `should create zaakrol for vestiging without handelsnaam`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()

        val plugin = zakenApiPluginAndMocksForZaakRol(
            zakenApiClient = zakenApiClient,
            executionMock = executionMock
        )

        plugin.createVestigingZaakRol(
            execution = executionMock,
            roltypeUrl = roltypeUrl(),
            rolToelichting = "rolToelichting",
            kvkNummer = "kvkNummer",
            vestigingsNummer = "vestigingsNummer",
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue
        assertThat(rol.zaak).isEqualTo(zaakUri())
        assertThat(rol.roltype).isEqualTo(roltypeUri())
        assertThat(rol.roltoelichting).isEqualTo("rolToelichting")

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolVestiging
        assertThat(betrokkeneIdentificatie.handelsnaam).isNull()
        assertThat(betrokkeneIdentificatie.kvkNummer).isEqualTo("kvkNummer")
        assertThat(betrokkeneIdentificatie.vestigingsNummer).isEqualTo("vestigingsNummer")
    }

    @Test
    fun `should create zaakrol for vestiging with handelsnaam`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()

        val plugin = zakenApiPluginAndMocksForZaakRol(
            zakenApiClient = zakenApiClient,
            executionMock = executionMock
        )

        plugin.createVestigingZaakRol(
            execution = executionMock,
            roltypeUrl = roltypeUrl(),
            rolToelichting = "rolToelichting",
            handelsnaam = "handelsnaam",
            kvkNummer = "kvkNummer",
            vestigingsNummer = "vestigingsNummer",
        )

        val rolCaptor = argumentCaptor<Rol>()
        verify(zakenApiClient).createZaakRol(any(), any(), rolCaptor.capture())

        val rol = rolCaptor.firstValue
        assertThat(rol.zaak).isEqualTo(zaakUri())
        assertThat(rol.roltype).isEqualTo(roltypeUri())
        assertThat(rol.roltoelichting).isEqualTo("rolToelichting")

        val betrokkeneIdentificatie = rol.betrokkeneIdentificatie as RolVestiging
        assertThat(betrokkeneIdentificatie.handelsnaam).isNotNull
        assertThat(betrokkeneIdentificatie.handelsnaam!!.first()).isEqualTo("handelsnaam")
        assertThat(betrokkeneIdentificatie.kvkNummer).isEqualTo("kvkNummer")
        assertThat(betrokkeneIdentificatie.vestigingsNummer).isEqualTo("vestigingsNummer")
    }

    private fun zakenApiPluginAndMocksForZaakRol(
        zakenApiClient: ZakenApiClient,
        executionMock: DelegateExecution
    ): ZakenApiPlugin {
        val zaakUrlProvider: ZaakUrlProvider = mock()

        val documentId = UUID.randomUUID()
        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(zaakUri())

        return zakenApiPlugin(
            zakenApiClient = zakenApiClient,
            zaakUrlProvider = zaakUrlProvider
        )
    }

    @Test
    fun `should create zaak with required properties only`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val rsin = Rsin("051845623")
        val zaaktypeUrl = zaaktypeUri()

        whenever(executionMock.businessKey)
            .thenReturn(documentId.toString())

        whenever(
            zakenApiClient.createZaak(
                authentication = eq(authenticationMock),
                baseUrl = eq(zakenApiUri()),
                request = any()
            )
        ).thenReturn(
            ZaakResponse(
                url = zaakUri(),
                uuid = UUID.randomUUID(),
                zaaktype = zaaktypeUrl,
                bronorganisatie = rsin,
                startdatum = LocalDate.now(),
                verantwoordelijkeOrganisatie = rsin,
            )
        )

        val plugin = zakenApiPlugin(
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock,
            pluginService = pluginServiceMock()
        )

        plugin.createZaak(
            execution = executionMock,
            rsin = rsin,
            zaaktypeUrl = zaaktypeUrl
        )

        val captor = argumentCaptor<CreateZaakRequest>()
        verify(zakenApiClient).createZaak(any(), any(), captor.capture())

        val request = captor.firstValue
        assertThat(request.bronorganisatie).isEqualTo(rsin)
        assertThat(request.zaaktype).isEqualTo(zaaktypeUrl)
        assertThat(request.verantwoordelijkeOrganisatie).isEqualTo(rsin)
        assertThat(request.startdatum).isNotNull()
        assertThat(request.omschrijving).isNull()
        assertThat(request.einddatumGepland).isNull()
        assertThat(request.toelichting).isNull()
        assertThat(request.uiterlijkeEinddatumAfdoening).isNull()
        assertThat(request.communicatiekanaal).isNull()
        assertThat(request.betalingsindicatie).isNull()
        assertThat(request.zaakgeometrie).isNull()
        assertThat(request.hoofdzaak).isNull()
    }

    @Test
    fun `should create zaak with additional properties`() {
        val zakenApiClient: ZakenApiClient = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val rsin = Rsin("051845623")
        val zaaktypeUrl = zaaktypeUri()
        val description = "Omschrijving"
        val plannedEndDate = LocalDate.now().plusDays(10)
        val finalDeliveryDate = null
        val explanation = "Toelichting"
        val communicationChannel = communicationChannel()
        val paymentIndication = Betalingsindicatie.GEDEELTELIJK.key
        val caseGeometryType = GeometryType.POINT.key
        val caseGeometryCoordinates = "[4.932921, 52.370085]"
        val mainCase = zaakUrl("123")

        whenever(executionMock.businessKey)
            .thenReturn(documentId.toString())

        whenever(
            zakenApiClient.createZaak(
                authentication = eq(authenticationMock),
                baseUrl = eq(zakenApiUri()),
                request = any()
            )
        ).thenReturn(
            ZaakResponse(
                url = zaakUri(),
                uuid = UUID.randomUUID(),
                zaaktype = zaaktypeUrl,
                bronorganisatie = rsin,
                startdatum = LocalDate.now(),
                verantwoordelijkeOrganisatie = rsin,
            )
        )

        val plugin = zakenApiPlugin(
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock,
            pluginService = pluginServiceMock()
        )

        plugin.createZaak(
            execution = executionMock,
            rsin = rsin,
            zaaktypeUrl = zaaktypeUrl,
            description = description,
            plannedEndDate = plannedEndDate.toString(),
            finalDeliveryDate = finalDeliveryDate,
            explanation = explanation,
            communicationChannel = communicationChannel,
            paymentIndication = paymentIndication,
            caseGeometryType = caseGeometryType,
            caseGeometryCoordinates = caseGeometryCoordinates,
            mainCase = mainCase
        )

        val captor = argumentCaptor<CreateZaakRequest>()
        verify(zakenApiClient).createZaak(any(), any(), captor.capture())

        val request = captor.firstValue
        assertThat(request.bronorganisatie).isEqualTo(rsin)
        assertThat(request.zaaktype).isEqualTo(zaaktypeUrl)
        assertThat(request.verantwoordelijkeOrganisatie).isEqualTo(rsin)
        assertThat(request.startdatum).isNotNull()
        assertThat(request.omschrijving).isEqualTo(description)
        assertThat(request.einddatumGepland).isEqualTo(plannedEndDate)
        assertThat(request.toelichting).isEqualTo(explanation)
        assertThat(request.uiterlijkeEinddatumAfdoening).isNull()
        assertThat(request.communicatiekanaal).isEqualTo(URI.create(communicationChannel))
        assertThat(request.betalingsindicatie).isEqualTo(Betalingsindicatie.GEDEELTELIJK)
        assertThat(request.zaakgeometrie).isEqualTo(Geometry(GeometryType.POINT, listOf(52.370216F, 4.895168F)))
        assertThat(request.hoofdzaak).isEqualTo(URI.create(mainCase))
    }

    @Test
    fun `should patch zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()
        val executionMock: DelegateExecution = mock()
        val authenticationMock: ZakenApiAuthentication = mock()

        val documentId = UUID.fromString("dff80fb1-e24e-4287-b168-7bb199be5d58")
        val zaakId = "f18146df-4b26-4a32-8e52-122cfa4475bd"
        val zaakUrl = zaakUri(zaakId)
        val zaakInstanceLink: ZaakInstanceLink = mock()
        val zaakResponse: ZaakResponse = mock()

        val description = "Omschrijving"
        val explantation = "Toelichting"
        val communicationChannel = communicationChannel()
        val communicationChannelName = "Communicatiekanaal Naam"
        val nowDate = LocalDate.parse("2025-07-23")
        val plannedEndDate = nowDate.plusMonths(10).toString()
        val finalDeliveryDate = nowDate.plusYears(1).toString()
        val publicationDate = nowDate.minusWeeks(2).toString()
        val paymentIndication = Betalingsindicatie.GEDEELTELIJK.key
        val lastPaymentDate = nowDate.minusWeeks(1).toString()
        val archiveActionDate = nowDate.plusYears(7).toString()
        val startDateRetentionPeriod = nowDate.plusYears(5).toString()
        val mainCase = zaakUrl("3a941618-b0f1-4a0e-a9d9-c9b25ef50eaf")
        val caseGeometryType = GeometryType.POINT.key
        val caseGeometryCoordinates = "[0.0, 1.0]"

        whenever(executionMock.businessKey)
            .thenReturn(documentId.toString())

        whenever(zaakInstanceLink.zaakInstanceUrl)
            .thenReturn(zaakUrl)

        whenever(zaakInstanceLinkRepository.findByDocumentId(eq(documentId)))
            .thenReturn(zaakInstanceLink)

        whenever(zaakResponse.url)
            .thenReturn(zaakUrl)

        whenever(zakenApiClient.patchZaak(
            authentication = eq(authenticationMock),
            baseUrl = eq(zakenApiUri()),
            zaakUrl = eq(zaakUrl),
            request = any<PatchZaakRequest>()
        ))
            .thenReturn(zaakResponse)

        val plugin = zakenApiPlugin(
            zakenApiClient = zakenApiClient,
            zaakInstanceLinkRepository = zaakInstanceLinkRepository,
            authenticationMock = authenticationMock,
            pluginService = pluginServiceMock()
        )

        plugin.patchZaak(
            execution = executionMock,
            description = description,
            explanation = explantation,
            plannedEndDate = plannedEndDate,
            finalDeliveryDate = finalDeliveryDate,
            publicationDate = publicationDate,
            communicationChannel = communicationChannel,
            communicationChannelName = communicationChannelName,
            paymentIndication = paymentIndication,
            lastPaymentDate = lastPaymentDate,
            caseGeometryType = caseGeometryType,
            caseGeometryCoordinates = caseGeometryCoordinates,
            mainCase = mainCase,
            archiveActionDate = archiveActionDate,
            startDateRetentionPeriod  = startDateRetentionPeriod
        )

        val captor = argumentCaptor<PatchZaakRequest>()
        verify(zakenApiClient).patchZaak(
            authentication = any(),
            baseUrl = any(),
            zaakUrl = any(),
            request = captor.capture()
        )

        val request = captor.firstValue
        assertThat(request.omschrijving).isEqualTo(description)
        assertThat(request.toelichting).isEqualTo(explantation)
        assertThat(request.einddatumGepland).isEqualTo(plannedEndDate)
        assertThat(request.uiterlijkeEinddatumAfdoening).isEqualTo(finalDeliveryDate)
        assertThat(request.publicatiedatum).isEqualTo(publicationDate)
        assertThat(request.communicatiekanaal).isEqualTo(URI.create(communicationChannel))
        assertThat(request.communicatiekanaalNaam).isEqualTo(communicationChannelName)
        assertThat(request.betalingsindicatie).isEqualTo(Betalingsindicatie.GEDEELTELIJK)
        assertThat(request.laatsteBetaaldatum).isEqualTo(lastPaymentDate)
        assertThat(request.zaakgeometrie).isEqualTo(Geometry(GeometryType.POINT, listOf(0.0F, 1.0F)))
        assertThat(request.hoofdzaak).isEqualTo(URI.create(mainCase))
        assertThat(request.archiefactiedatum).isEqualTo(archiveActionDate)
        assertThat(request.startdatumBewaartermijn).isEqualTo(startDateRetentionPeriod)
    }

    @Test
    fun `should create zaak status`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()
        val statustypeUrl = statustypeUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

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
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()
        val resultaatTypeUrl = resultaatTypeUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        plugin.createZaakResultaat(executionMock, resultaatTypeUrl, "Result description")

        val captor = argumentCaptor<CreateZaakResultaatRequest>()
        verify(zakenApiClient).createZaakResultaat(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(zaakUrl, request.zaak)
        assertEquals(resultaatTypeUrl, request.resultaattype)
        assertEquals("Result description", request.toelichting)
    }

    @Test
    fun `should update zaakopschorting and verlenging`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        // when
        plugin.setZaakOpschorting(
            execution = executionMock,
            verlengingsduur = "P3Y",
            toelichtingVerlenging = "testing verlenging",
            toelichtingOpschorting = "testing opschorting"
        )

        // then
        val captor = argumentCaptor<ZaakopschortingRequest>()
        verify(zakenApiClient).setZaakOpschorting(any(), any(), captor.capture())

        val request = captor.firstValue
        assertTrue(request.opschorting.indicatie)
        assertEquals("testing verlenging", request.verlenging.reden)
        assertEquals("testing opschorting", request.opschorting.reden)
    }

    @Test
    fun `should start hersteltermijn`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)
        whenever(zakenApiClient.getZaak(authenticationMock, zaakUrl))
            .thenReturn(
                ZaakResponse(
                    uuid = UUID.randomUUID(),
                    url = zaakUrl,
                    bronorganisatie = Rsin("051845623"),
                    startdatum = LocalDate.now(),
                    verantwoordelijkeOrganisatie = Rsin("051845623"),
                    zaaktype = URI("www.ritense.com"),
                    uiterlijkeEinddatumAfdoening = LocalDate.parse("2050-01-01")
                )
            )

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        // when
        plugin.startHersteltermijn(
            execution = executionMock,
            maxDurationInDays = 14
        )

        // then
        val captor = argumentCaptor<PatchZaakRequest>()
        verify(zakenApiClient).patchZaak(any(), any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(LocalDate.parse("2050-01-15"), request.uiterlijkeEinddatumAfdoening)
        assertTrue(request.opschorting!!.indicatie)
        assertEquals("hersteltermijn", request.opschorting.reden)
    }

    @Test
    fun `should not start hersteltermijn twice`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val zaakHersteltermijnRepository: ZaakHersteltermijnRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()
        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)
        whenever(zakenApiClient.getZaak(authenticationMock, zaakUrl))
            .thenReturn(
                ZaakResponse(
                    uuid = UUID.randomUUID(),
                    url = zaakUrl,
                    bronorganisatie = Rsin("051845623"),
                    startdatum = LocalDate.now(),
                    verantwoordelijkeOrganisatie = Rsin("051845623"),
                    zaaktype = URI("www.ritense.com"),
                    uiterlijkeEinddatumAfdoening = LocalDate.parse("2050-01-01")
                )
            )
        whenever(zaakHersteltermijnRepository.findByZaakUrlAndEndDateIsNull(zaakUrl))
            .thenReturn(ZaakHersteltermijn(UUID.randomUUID(), zaakUrl, LocalDate.now(), null, 12))

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            zaakHersteltermijnRepository = zaakHersteltermijnRepository,
            authenticationMock = authenticationMock
        )

        assertEquals(
            "Hersteltermijn already exists for zaak '$zaakUrl'",
            assertThrows<IllegalArgumentException> {
                plugin.startHersteltermijn(
                    execution = executionMock,
                    maxDurationInDays = 14
                )
            }.message
        )
    }

    @Test
    fun `should end hersteltermijn`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val zaakHersteltermijnRepository: ZaakHersteltermijnRepository = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)
        whenever(zakenApiClient.getZaak(authenticationMock, zaakUrl))
            .thenReturn(
                ZaakResponse(
                    uuid = UUID.randomUUID(),
                    url = zaakUrl,
                    bronorganisatie = Rsin("051845623"),
                    startdatum = LocalDate.now().minusDays(50),
                    verantwoordelijkeOrganisatie = Rsin("051845623"),
                    zaaktype = URI("www.ritense.com"),
                    uiterlijkeEinddatumAfdoening = LocalDate.now().plusDays(50)
                )
            )
        whenever(zaakHersteltermijnRepository.findByZaakUrlAndEndDateIsNull(zaakUrl)).thenReturn(ZaakHersteltermijn(
            zaakUrl = zaakUrl,
            startDate = LocalDate.now().minusDays(8),
            endDate =  null,
            maxDurationInDays = 17
        ))

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            zaakHersteltermijnRepository = zaakHersteltermijnRepository,
            authenticationMock = authenticationMock
        )

        // when
        plugin.endHersteltermijn(
            execution = executionMock
        )

        // then
        val captor = argumentCaptor<PatchZaakRequest>()
        verify(zakenApiClient).patchZaak(any(), any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(LocalDate.now().plusDays(50 - 17 + 8), request.uiterlijkeEinddatumAfdoening)
        assertFalse(request.opschorting!!.indicatie)
        assertEquals("", request.opschorting.reden)
    }

    @Test
    fun `should create zaakeigenschap`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()
        val eigenschapUrl = eigenschapUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)
        whenever(zakenApiClient.createZaakeigenschap(any(), any(), any()))
            .thenReturn(
                ZaakeigenschapResponse(
                    url =  URI("${zaakUrl()}/zaakeigenschappen/5678"),
                    zaak = zaakUrl,
                    eigenschap = eigenschapUrl,
                    waarde = "test-value"
                )
            )

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        // when
        plugin.createZaakeigenschap(
            execution = executionMock,
            eigenschapUrl = eigenschapUrl,
            eigenschapValue = "test-value"
        )

        // then
        val captor = argumentCaptor<CreateZaakeigenschapRequest>()
        verify(zakenApiClient).createZaakeigenschap(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(zaakUrl, request.zaak)
        assertEquals(eigenschapUrl, request.eigenschap)
        assertEquals("test-value", request.waarde)
    }

    @Test
    fun `should update zaakeigenschap`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()
        val eigenschapUrl = eigenschapUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)
        whenever(zakenApiClient.getZaakeigenschappen(any(), any(), any()))
            .thenReturn(
                listOf(
                    ZaakeigenschapResponse(
                        url = URI("${zaakUrl()}/zaakeigenschappen/5678"),
                        zaak = zaakUrl,
                        eigenschap = eigenschapUrl,
                        waarde = "test-value"
                    )
                )
            )

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        // when
        plugin.updateZaakeigenschap(
            execution = executionMock,
            eigenschapUrl = eigenschapUrl,
            eigenschapValue = "test-value-updated"
        )

        // then
        val captor = argumentCaptor<UpdateZaakeigenschapRequest>()
        verify(zakenApiClient).updateZaakeigenschap(any(), any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(zaakUrl, request.zaak)
        assertEquals(eigenschapUrl, request.eigenschap)
        assertEquals("test-value-updated", request.waarde)
    }

    @Test
    fun `should delete zaakeigenschap`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()
        val eigenschapUrl = eigenschapUri()

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)
        whenever(zakenApiClient.getZaakeigenschappen(any(), any(), any()))
            .thenReturn(
                listOf(
                    ZaakeigenschapResponse(
                        url = URI("${zaakUrl()}/zaakeigenschappen/5678"),
                        zaak = zaakUrl,
                        eigenschap = eigenschapUrl,
                        waarde = "test-value"
                    )
                )
            )

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        // when
        plugin.deleteZaakeigenschap(
            execution = executionMock,
            eigenschapUrl = eigenschapUrl
        )

        // then
        verify(zakenApiClient).deleteZaakeigenschap(any(), any(), any())
    }

    @Test
    fun `should relateer zaken`() {

        // given
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()
        val executionMock = mock<DelegateExecution>()
        val zaakResponseMock: ZaakResponse = mock()

        val documentId = UUID.randomUUID()
        val zaakUrl = zaakUri()

        val teRelaterenZaakUri = zaakUri("7cbc216f-0fa9-40e8-92b0-00399b5340f8")
        val aardRelatie = "vervolg"

        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)
        whenever(zaakResponseMock.relevanteAndereZaken).thenReturn(listOf())
        whenever(zakenApiClient.getZaak(authenticationMock, zaakUrl)).thenReturn(zaakResponseMock)

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        // when
        plugin.relateerZaken(
            execution = executionMock,
            teRelaterenZaakUri = teRelaterenZaakUri,
            aardRelatie = aardRelatie
        )

        // then
        val captor = argumentCaptor<PatchZaakRequest>()
        verify(zakenApiClient).patchZaak(
            eq(authenticationMock),
            eq(plugin.url),
            eq(zaakUrl),
            captor.capture())

        val relevanteAndereZaken = captor.firstValue.relevanteAndereZaken
        assertThat(relevanteAndereZaken).hasSize(1)
        assertThat(relevanteAndereZaken!![0].url).isEqualTo(teRelaterenZaakUri)
        assertThat(relevanteAndereZaken[0].aardRelatie).isEqualTo(AardRelatie.VERVOLG)
    }

    @Test
    fun `should link object to zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(zaakUri())

        val plugin = zakenApiPlugin(
            zaakUrlProvider = zaakUrlProvider,
            zakenApiClient = zakenApiClient,
            authenticationMock = authenticationMock
        )

        plugin.createZaakObject(zaakUri(), objectUri(), "zaakdetails", documentId)

        val captor = argumentCaptor<ZaakObjectRequest>()
        verify(zakenApiClient).createZaakObject(any(), any(), captor.capture())

        val request = captor.firstValue
        assertEquals(zaakUrl(), request.zaakUrl.toString())
        assertEquals(objectUrl(), request.objectUrl.toString())
    }

    private fun zakenApiPlugin(
        url: URI = zakenApiUri(),
        zaakUrlProvider: ZaakUrlProvider = mock(),
        zakenApiClient: ZakenApiClient = mock(),
        storageService: TemporaryResourceStorageService = mock(),
        zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock(),
        pluginService: PluginService = mock(),
        zaakHersteltermijnRepository: ZaakHersteltermijnRepository = mock(),
        platformTransactionManager: PlatformTransactionManager = mock(),
        authenticationMock: ZakenApiAuthentication = mock()
    ): ZakenApiPlugin {
        return ZakenApiPlugin(
            zakenApiClient,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository,
            pluginService,
            zaakHersteltermijnRepository,
            platformTransactionManager
        ).apply {
            this.url = url
            this.authenticationPluginConfiguration = authenticationMock
        }
    }

    private fun pluginServiceMock(): PluginService = mock {
        on { this.getObjectMapper()  } doReturn MapperSingleton.get()
    }

    private fun zakenApiUrl() = "https://zaken.plugin.url"
    private fun zakenApiUri() = URI(zakenApiUrl())

    private fun zaakUrl(id: String = "e1e96e94-e7ff-47d1-9ea1-7c7c81713480") = "${zakenApiUrl()}/zaken/$id"
    private fun zaakUri(id: String = "e1e96e94-e7ff-47d1-9ea1-7c7c81713480") = URI(zaakUrl(id))

    private fun zaaktypeUrl(id: String = "e31e478c-97d5-4164-8e62-12a84a573eba") = "${zakenApiUrl()}/zaaktypen/$id"
    private fun zaaktypeUri(id: String = "e31e478c-97d5-4164-8e62-12a84a573eba") = URI(zaaktypeUrl(id))

    private fun statustypeUrl(id: String = "94cbae11-df23-41f0-9e6a-d122dd9a7a50") = "${zakenApiUrl()}/statustypen/$id"
    private fun statustypeUri(id: String = "94cbae11-df23-41f0-9e6a-d122dd9a7a50") = URI(statustypeUrl(id))

    private fun eigenschapUrl(id: String = "626d6b83-1aeb-478f-87b1-898370342c07") = "${zakenApiUrl()}/eigenschappen/$id"
    private fun eigenschapUri(id: String = "626d6b83-1aeb-478f-87b1-898370342c07") = URI(eigenschapUrl(id))

    private fun roltypeUrl(id: String = "a860b0ab-47ca-4471-bff6-6fb53c760f07") = "${zakenApiUrl()}/roltypen/$id"
    private fun roltypeUri(id: String = "a860b0ab-47ca-4471-bff6-6fb53c760f07") = URI(roltypeUrl(id))

    private fun resultaatTypeUrl(id: String = "e85ae64e-4083-44a0-b512-e21c6114bd58") = "${zakenApiUrl()}/resultaattypen/$id"
    private fun resultaatTypeUri(id: String = "e85ae64e-4083-44a0-b512-e21c6114bd58") = URI(resultaatTypeUrl(id))

    private fun objectUrl() = "https://object.url"
    private fun objectUri() = URI(objectUrl())

    private fun documentUrl() = "https://document.url"

    private fun communicationChannel() = "https://example.com/comminicatiekanaal/example"
}
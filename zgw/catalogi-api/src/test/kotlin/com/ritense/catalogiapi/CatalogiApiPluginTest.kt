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

package com.ritense.catalogiapi

import com.ritense.catalogiapi.client.CatalogiApiClient
import com.ritense.catalogiapi.client.ZaaktypeInformatieobjecttypeRequest
import com.ritense.catalogiapi.domain.Besluittype
import com.ritense.catalogiapi.domain.Eigenschap
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Resultaattype
import com.ritense.catalogiapi.domain.Specificatie
import com.ritense.catalogiapi.domain.Statustype
import com.ritense.catalogiapi.domain.Zaaktype
import com.ritense.catalogiapi.domain.ZaaktypeInformatieobjecttype
import com.ritense.catalogiapi.exception.BesluittypeNotFoundException
import com.ritense.catalogiapi.exception.EigenschapNotFoundException
import com.ritense.catalogiapi.exception.ResultaattypeNotFoundException
import com.ritense.catalogiapi.exception.StatustypeNotFoundException
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.catalogiapi.web.rest.result.ResultaattypeDto
import com.ritense.catalogiapi.web.rest.result.StatustypeDto
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.zgw.Page
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

internal class CatalogiApiPluginTest : BaseTest() {

    private lateinit var client: CatalogiApiClient
    private lateinit var zaaktypeUrlProvider: ZaaktypeUrlProvider
    private lateinit var documentService: DocumentService
    private lateinit var plugin: CatalogiApiPlugin

    private val caseDefinitionId = CaseDefinitionId("test", "1.0.0")

    @BeforeEach
    fun setUp() {
        client = mock()
        zaaktypeUrlProvider = mock()
        documentService = mock()

        plugin = CatalogiApiPlugin(client, zaaktypeUrlProvider, documentService).apply {
            authenticationPluginConfiguration = mock()
            url = mock()
        }
    }

    @Test
    fun `should call client to get informatieobjecttypes`() {
        val zaakTypeUrl = zaaktypeUrl().toURI()
        val resultPage = mock<Page<ZaaktypeInformatieobjecttype>>()

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                request = ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                )
            )
        ).thenReturn(resultPage)

        val informatieobjecttypeUrl1 = informatieObjectTypeUrl("1").toURI()
        val mockZaaktypeInformatieobjecttype1 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl1)
        val mockInformatieobjecttype1 = mockInformatieObjectType()

        val informatieobjecttypeUrl2 = informatieObjectTypeUrl("2").toURI()
        val mockZaaktypeInformatieobjecttype2 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl2)
        val mockInformatieobjecttype2 = mockInformatieObjectType()

        whenever(resultPage.results)
            .thenReturn(
                listOf(
                    mockZaaktypeInformatieobjecttype1,
                    mockZaaktypeInformatieobjecttype2
                )
        )

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl1
            )
        ).thenReturn(mockInformatieobjecttype1)

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl2
            )
        ).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(2, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype1, informatieobjecttypes[0])
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[1])
    }

    @Test
    fun `should call client to get informatieobjecttypes with multiple pages`() {
        val zaakTypeUrl = zaaktypeUrl().toURI()
        val resultPage1 = mock<Page<ZaaktypeInformatieobjecttype>>()
        val resultPage2 = mock<Page<ZaaktypeInformatieobjecttype>>()

        whenever(resultPage1.next)
            .thenReturn(zaaktypeUrl("2").toURI())

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                request = ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                )
            )
        ).thenReturn(resultPage1)

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                request = ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 2
                )
            )
        ).thenReturn(resultPage2)

        val informatieobjecttypeUrl1 = informatieObjectTypeUrl("1").toURI()
        val mockZaaktypeInformatieobjecttype1 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl1)
        val mockInformatieobjecttype1 = mockInformatieObjectType()

        val informatieobjecttypeUrl2 = informatieObjectTypeUrl("2").toURI()
        val mockZaaktypeInformatieobjecttype2 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl2)
        val mockInformatieobjecttype2 = mockInformatieObjectType()

        whenever(resultPage1.results)
            .thenReturn(listOf(mockZaaktypeInformatieobjecttype1))

        whenever(resultPage2.results)
            .thenReturn(listOf(mockZaaktypeInformatieobjecttype2))

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl1
            )
        ).thenReturn(mockInformatieobjecttype1)

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl2
            )
        ).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(2, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype1, informatieobjecttypes[0])
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[1])
    }

    @Test
    fun `should filter informatieobjecttypes that are still in concept`() {
        val zaakTypeUrl = zaaktypeUrl().toURI()
        val resultPage = mock<Page<ZaaktypeInformatieobjecttype>>()

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                request = ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                )
            )
        ).thenReturn(resultPage)

        val informatieobjecttypeUrl1 = informatieObjectTypeUrl("1").toURI()
        val mockZaaktypeInformatieobjecttype1 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl1)
        val mockInformatieobjecttype1 = mockInformatieObjectType(concept = true)

        val informatieobjecttypeUrl2 = informatieObjectTypeUrl("2").toURI()
        val mockZaaktypeInformatieobjecttype2 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl2)
        val mockInformatieobjecttype2 = mockInformatieObjectType()

        whenever(resultPage.results).thenReturn(
            listOf(
                mockZaaktypeInformatieobjecttype1,
                mockZaaktypeInformatieobjecttype2
            )
        )

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl1
            )
        ).thenReturn(mockInformatieobjecttype1)

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl2
            )
        ).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(1, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[0])
    }

    @Test
    fun `should filter informatieobjecttypes that are not valid yet`() {
        val zaakTypeUrl = zaaktypeUrl().toURI()
        val resultPage = mock<Page<ZaaktypeInformatieobjecttype>>()

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                request = ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                )
            )
        ).thenReturn(resultPage)

        val informatieobjecttypeUrl1 = informatieObjectTypeUrl("1").toURI()
        val mockZaaktypeInformatieobjecttype1 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl1)
        val mockInformatieobjecttype1 = mockInformatieObjectType(
            beginGeldigheid = LocalDate.now().plusDays(1)
        )

        val informatieobjecttypeUrl2 = informatieObjectTypeUrl("2").toURI()
        val mockZaaktypeInformatieobjecttype2 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl2)
        val mockInformatieobjecttype2 = mockInformatieObjectType()

        whenever(resultPage.results).thenReturn(
            listOf(
                mockZaaktypeInformatieobjecttype1,
                mockZaaktypeInformatieobjecttype2
            )
        )

        mockGetInformatieobjecttype(
            informatieObjectTypeUrl = informatieobjecttypeUrl1,
            informatieObjectType = mockInformatieobjecttype1
        )

        mockGetInformatieobjecttype(
            informatieObjectTypeUrl = informatieobjecttypeUrl2,
            informatieObjectType = mockInformatieobjecttype2
        )

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(1, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[0])
    }

    @Test
    fun `should filter informatieojecttypes that are no longer valid`() {
        val zaakTypeUrl = zaaktypeUrl().toURI()
        val resultPage = mock<Page<ZaaktypeInformatieobjecttype>>()

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                request = ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                )
            )
        ).thenReturn(resultPage)

        val informatieobjecttypeUrl1 = informatieObjectTypeUrl("1").toURI()
        val mockZaaktypeInformatieobjecttype1 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl1)
        val mockInformatieobjecttype1 = mockInformatieObjectType(
            beginGeldigheid = LocalDate.now().minusWeeks(1),
            eindeGeldigheid = LocalDate.now().minusDays(1)
        )

        val informatieobjecttypeUrl2 = informatieObjectTypeUrl("2").toURI()
        val mockInformatieobjecttype2 = mockInformatieObjectType()
        val mockZaaktypeInformatieobjecttype2 = mockZaaktypeInformatieObjectType(informatieobjecttypeUrl2)

        whenever(resultPage.results).thenReturn(
            listOf(
                mockZaaktypeInformatieobjecttype1,
                mockZaaktypeInformatieobjecttype2
            )
        )

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl1
            )
        ).thenReturn(mockInformatieobjecttype1)

        whenever(
            client.getInformatieobjecttype(
                authentication = plugin.authenticationPluginConfiguration,
                baseUrl = plugin.url,
                informatieobjecttypeUrl = informatieobjecttypeUrl2
            )
        ).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(1, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[0])
    }

    @Test
    fun `should get status typen for zaaktype specified via property`() {
        // given
        val processVariable = "statusTypenProcessVar"
        val zaaktypeUrl = zaaktypeUrl()
        val execution = mockExecution()

        mockStatustypen(zaaktypeUrl.toURI())

        // when
        plugin.getStatustypen(
            execution = execution,
            processVariable = processVariable,
            zaaktypeUrl = zaaktypeUrl
        )

        // then
        verify(execution, times(1))
            .setVariable(eq(processVariable), any<List<StatustypeDto>>())
    }

    @Test
    fun `should get status typen for zaaktype via linked zaak`() {
        // given
        val processVariable = "statusTypenProcessVar"
        val zaaktypeUrl = zaaktypeUrl()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl.toURI())
        mockStatustypen(zaaktypeUrl.toURI())

        // when
        plugin.getStatustypen(
            execution = execution,
            processVariable = processVariable,
            zaaktypeUrl = zaaktypeUrl
        )

        // then
        verify(execution, times(1))
            .setVariable(eq(processVariable), any<List<StatustypeDto>>())
    }

    @Test
    fun `should get status type`() {
        val statustype = "Registered"
        val processVariable = "myProcessVar"
        val statustypeUrl = statustypeUrl()
        val zaaktypeUrl = zaaktypeUrl().toURI()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl)
        mockStatustypen(
            zaaktypeUrl = zaaktypeUrl,
            additionalStatustypen = listOf(
                statusType(
                    url = statustypeUrl.toURI(),
                    zaaktypeUrl = zaaktypeUrl,
                    omschrijving = statustype
                )
            )
        )

        plugin.getStatustype(
            execution = execution,
            statustype = statustype,
            processVariable = processVariable
        )

        verify(execution, times(1))
            .setVariable(eq(processVariable), eq(statustypeUrl))
    }

    @Test
    fun `should throw StatustypeNotFoundException when get status type doesn't exist`() {
        val statustype = "Registered"
        val zaaktypeUrl = zaaktypeUrl()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl.toURI())
        mockStatustypen(
            zaaktypeUrl = zaaktypeUrl.toURI(),
            defaultStatustypen = listOf()
        )

        val exception = assertThrows<StatustypeNotFoundException> {
            plugin.getStatustype(
                execution = execution,
                statustype = statustype,
                processVariable = "myProcessVar"
            )
        }

        assertEquals("No statustype was found with 'omschrijving': 'Registered'", exception.message)
    }

    private fun mockStatustypen(
        zaaktypeUrl: URI,
        defaultStatustypen: List<Statustype> = listOf(
            statusType(
                url = statustypeUrl("1").toURI(),
                zaaktypeUrl = zaaktypeUrl,
                omschrijving = "first status"
            ),
            statusType(
                url = statustypeUrl("2").toURI(),
                zaaktypeUrl = zaaktypeUrl,
                omschrijving = "second status"
            )
        ),
        additionalStatustypen: List<Statustype> = listOf()
    ) {
        whenever(client.getStatustypen(any(), any(), any()))
            .thenReturn(
                Page(
                    count = defaultStatustypen.size.plus(additionalStatustypen.size),
                    results = defaultStatustypen.plus(additionalStatustypen)
                )
            )
    }

    @Test
    fun `should get resultaat typen for zaaktype specified via property`() {
        // given
        val processVariable = "resultaatTypenProcessVar"
        val zaaktypeUrl = zaaktypeUrl()
        val execution = mockExecution()

        mockResultaattypen(zaaktypeUrl.toURI())

        // when
        plugin.getResultaattypen(
            execution = execution,
            processVariable = processVariable,
            zaaktypeUrl = zaaktypeUrl
        )

        // then
        verify(execution, times(1))
            .setVariable(eq(processVariable), any<List<ResultaattypeDto>>())
    }

    @Test
    fun `should get resultaat typen for zaaktype via linked zaak`() {
        // given
        val processVariable = "resultaatTypenProcessVar"
        val zaaktypeUrl = zaaktypeUrl()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl.toURI())
        mockResultaattypen(zaaktypeUrl.toURI())

        // when
        plugin.getResultaattypen(
            execution = execution,
            processVariable = processVariable,
            zaaktypeUrl = zaaktypeUrl
        )

        // then
        verify(execution, times(1))
            .setVariable(eq(processVariable), any<List<ResultaattypeDto>>())
    }

    @Test
    fun `should get resultaat type`() {
        val resultaattype = "Registered"
        val processVariable = "resultaatTypeProcessVar"
        val resultaattypeUrl = resultaatTypeUrl("4")
        val zaaktypeUrl = zaaktypeUrl().toURI()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl)
        mockResultaattypen(
            zaaktypeUrl = zaaktypeUrl,
            additionalResultaatTypen = listOf(
                resultaatType(
                    url = resultaattypeUrl.toURI(),
                    zaaktypeUrl = zaaktypeUrl,
                    omschrijving = resultaattype,
                    resultaatTypeOmschrijving = resultaatTypeOmschrijvingUrl().toURI(),
                    selectielijstKlasse = selectielijstKlasseUrl().toURI()
                )
            )
        )

        plugin.getResultaattype(
            execution = execution,
            resultaattype = resultaattype,
            processVariable = processVariable
        )

        verify(execution, times(1))
            .setVariable(eq(processVariable), eq(resultaattypeUrl))
    }

    @Test
    fun `should throw ResultaattypeNotFoundException when get resultaat type doesn't exist`() {
        val resultaattype = "Registered"
        val zaaktypeUrl = zaaktypeUrl()

        mockResultaattypen(
            zaaktypeUrl = zaaktypeUrl.toURI(),
            defaultResultaatTypen = listOf()
        )

        val exception = assertThrows<ResultaattypeNotFoundException> {
            plugin.getResultaattypeByOmschrijving(
                zaakTypeUrl = zaaktypeUrl.toURI(),
                omschrijving = resultaattype
            )
        }

        assertEquals("No resultaattype was found with 'omschrijving': '$resultaattype'", exception.message)
    }

    private fun mockResultaattypen(
        zaaktypeUrl: URI,
        defaultResultaatTypen: List<Resultaattype> = listOf(
            resultaatType(
                url = resultaatTypeUrl("1").toURI(),
                zaaktypeUrl = zaaktypeUrl,
                omschrijving = "first resultaat",
                resultaatTypeOmschrijving = resultaatTypeOmschrijvingUrl("1").toURI(),
                selectielijstKlasse = selectielijstKlasseUrl("1").toURI()
            ),
            resultaatType(
                url = resultaatTypeUrl("2").toURI(),
                zaaktypeUrl = zaaktypeUrl,
                omschrijving = "second resultaat",
                resultaatTypeOmschrijving = resultaatTypeOmschrijvingUrl("2").toURI(),
                selectielijstKlasse = selectielijstKlasseUrl("2").toURI()
            )
        ),
        additionalResultaatTypen: List<Resultaattype> = listOf()
    ) {
        whenever(client.getResultaattypen(any(), any(), any()))
            .thenReturn(
                Page(
                    count = defaultResultaatTypen.size.plus(additionalResultaatTypen.size),
                    results = defaultResultaatTypen.plus(additionalResultaatTypen)
                )
        )
    }

    @Test
    fun `should get besluit type`() {
        val besluittype = "Allocated"
        val processVariable = "myProcessVar"
        val besluittypeUrl = besluitTypeUrl("2")
        val zaaktypeUrl = zaaktypeUrl()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl.toURI())
        whenever(client.getBesluittypen(any(), any(), any())).thenReturn(
            Page(
                count = 3,
                results = listOf(
                    besluitType(
                        url = besluitTypeUrl("1").toURI(),
                        catalogusUrl = zaaktypeUrl.toURI(),
                        omschrijving = "other besluit"
                    ),
                    besluitType(
                        url = besluittypeUrl.toURI(),
                        catalogusUrl = zaaktypeUrl.toURI(),
                        omschrijving = besluittype
                    ),
                    besluitType(
                        url = besluitTypeUrl("3").toURI(),
                        catalogusUrl = zaaktypeUrl.toURI(),
                        omschrijving = "yet another besluit"
                    )
                )
            )
        )

        plugin.getBesluittype(
            execution, besluittype, processVariable
        )

        verify(execution, times(1))
            .setVariable(eq(processVariable), eq(besluittypeUrl))
    }

    @Test
    fun `should get besluit type by url`() {
        val besluittype = besluitTypeUrl()
        val processVariable = "myProcessVar"
        val documentId = documentId()
        val execution = mockExecution(documentId)

        plugin.getBesluittype(execution, besluittype, processVariable)

        verify(execution, times(1)).setVariable(processVariable, besluittype)
    }

    @Test
    fun `should throw BesluitypeNotFound exception when get besluit type doesn't exist`() {
        val besluittype = "Allocated"
        val processVariable = "myProcessVar"
        val zaaktypeUrl = zaaktypeUrl()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl.toURI())
        whenever(client.getBesluittypen(any(), any(), any()))
            .thenReturn(
                Page(count = 0, results = listOf())
            )

        val exception = assertThrows<BesluittypeNotFoundException> {
            plugin.getBesluittype(execution, besluittype, processVariable)
        }

        assertEquals("No besluittype was found with 'omschrijving': 'Allocated'", exception.message)
    }

    @Test
    fun `should get zaaktypen`() {
        whenever(client.getZaaktypen(any(), any(), any()))
            .thenReturn(
                Page(
                    1, URI(""), null, listOf(
                        newZaaktype(URI("zaak:1"), "Zaak 1", "zaak 1")
                    )
                )
            )
            .thenReturn(
                Page(
                    1, null, URI(""), listOf(
                        newZaaktype(URI("zaak:2"), "Zaak 2", "zaak 2")
                    )
                )
            )

        val zaaktypen = plugin.getZaaktypen()

        assertThat(zaaktypen).hasSize(2)
        zaaktypen.forEachIndexed { i: Int, zaaktype: Zaaktype ->
            val zaakNr = i + 1
            assertThat(zaaktype.url.toString()).isEqualTo("zaak:$zaakNr")
            assertThat(zaaktype.omschrijving).isEqualTo("Zaak $zaakNr")
            assertThat(zaaktype.omschrijvingGeneriek).isEqualTo("zaak $zaakNr")
        }
    }

    @Test
    fun `should get eigenschap`() {
        val eigenschapNaam = "Einddatum"
        val processVariable = "eigenschapUrlPv"
        val eigenschapUrl = eigenschapUrl("1")
        val zaaktypeUrl = zaaktypeUrl()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl.toURI())
        whenever(client.getEigenschappen(any(), any(), any())).thenReturn(
            Page(
                count = 3,
                results = listOf(
                    Eigenschap(
                        eigenschapUrl.toURI(),
                        eigenschapNaam,
                        "Einddatum",
                        Specificatie(null, "tekst", "lengte", "1:N", null),
                        null,
                        URI(zaaktypeUrl)
                    ),
                    Eigenschap(
                        eigenschapUrl("1").toURI(),
                        "startdatum",
                        "startdatum",
                        Specificatie(null, "tekst", "lengte", "1:N", null),
                        null,
                        URI(zaaktypeUrl)
                    ),
                    Eigenschap(
                        eigenschapUrl("3").toURI(),
                        "status",
                        "status",
                        Specificatie(null, "tekst", "lengte", "1:N", null),
                        null,
                        URI(zaaktypeUrl)
                    )
                )
            )
        )
        plugin.getEigenschap(execution, eigenschapNaam, processVariable)

        verify(execution, times(1))
            .setVariable(eq(processVariable), eq(eigenschapUrl))
    }

    @Test
    fun `should throw EigenschapNotFound exception when get eigenschap doesn't exist`() {
        val eigenschapNaam = "Einddatum"
        val processVariable = "eigenschapUrlPv"
        val zaaktypeUrl = zaaktypeUrl()
        val documentId = documentId()
        val document = mockDocument(documentId.toUUID())
        val execution = mockExecution(documentId)

        mockDocumentService(documentId, document)
        mockZaaktypeUrlProvider(zaaktypeUrl.toURI())
        whenever(client.getEigenschappen(any(), any(), any())).thenReturn(
            Page(count = 0, results = listOf())
        )

        val exception = assertThrows<EigenschapNotFoundException> {
            plugin.getEigenschap(execution, eigenschapNaam, processVariable)
        }

        assertEquals("No eigenschap was found with eigenschapnaam: 'Einddatum'", exception.message)
    }

    private fun mockDocument(documentId: UUID = documentId().toUUID()): JsonSchemaDocument = mock {
        on { this.definitionId() } doReturn JsonSchemaDocumentDefinitionId.of("myDocDef", caseDefinitionId)
        on { this.id } doReturn JsonSchemaDocumentId.existingId(documentId)
    }

    private fun mockInformatieObjectType(
        concept: Boolean = false,
        beginGeldigheid: LocalDate = LocalDate.now().minusDays(1),
        eindeGeldigheid: LocalDate? = null
    ): Informatieobjecttype = mock {
        on { this.concept} doReturn concept
        on { this.beginGeldigheid } doReturn beginGeldigheid
        on { this.eindeGeldigheid } doReturn eindeGeldigheid
    }

    private fun mockZaaktypeInformatieObjectType(informatieObjectTypeUrl: URI): ZaaktypeInformatieobjecttype = mock {
        on { this.informatieobjecttype } doReturn informatieObjectTypeUrl
    }

    private fun mockExecution(businessKey: String = documentId()): DelegateExecution = mock {
        on { this.businessKey } doReturn businessKey
    }

    private fun mockDocumentService(
        documentId: String = documentId(),
        document: JsonSchemaDocument = mockDocument()
    ) {
        whenever(documentService.get(eq(documentId)))
            .thenReturn(document)
    }

    private fun mockZaaktypeUrlProvider(zaaktypeUrl: URI = zaaktypeUrl().toURI()) {
        whenever(zaaktypeUrlProvider.getZaaktypeUrl(eq(caseDefinitionId)))
            .thenReturn(zaaktypeUrl)
    }

    private fun mockGetInformatieobjecttype(
        informatieObjectTypeUrl: URI,
        informatieObjectType: Informatieobjecttype
    ) {
        whenever(
            client.getInformatieobjecttype(
                authentication = eq(plugin.authenticationPluginConfiguration),
                baseUrl = eq(plugin.url),
                informatieobjecttypeUrl = eq(informatieObjectTypeUrl)
            )
        ).thenReturn(informatieObjectType)
    }

    private fun mockGetZaaktypeInformatieobjectTypes(
        zaakTypeUrl: URI,
        resultPage: Page<ZaaktypeInformatieobjecttype>
    ) {
        whenever(
            client.getZaaktypeInformatieobjecttypes(
                authentication = eq(plugin.authenticationPluginConfiguration),
                baseUrl = eq(plugin.url),
                request = eq(ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                ))
            )
        ).thenReturn(resultPage)
    }

    private fun besluitType(
        url: URI,
        catalogusUrl: URI,
        omschrijving: String
    ) = Besluittype(
        url = url,
        catalogus = catalogusUrl,
        zaaktypen = listOf(),
        omschrijving = omschrijving,
        omschrijvingGeneriek = null,
        besluitcategorie = null,
        reactietermijn = null,
        publicatieIndicatie = true,
        publicatietekst = null,
        publicatietermijn = null,
        toelichting = null,
        informatieobjecttypen = listOf(),
        beginGeldigheid = LocalDate.now().minusWeeks(1),
        eindeGeldigheid = null,
        concept = null
    )

    private fun resultaatType(
        url: URI,
        zaaktypeUrl: URI,
        omschrijving: String,
        resultaatTypeOmschrijving: URI,
        selectielijstKlasse: URI
    ) = Resultaattype(
        url = url,
        zaaktype = zaaktypeUrl,
        omschrijving = omschrijving,
        resultaattypeomschrijving = resultaatTypeOmschrijving,
        omschrijvingGeneriek = null,
        selectielijstklasse = selectielijstKlasse,
        toelichting = null
    )

    private fun statusType(
        url: URI,
        zaaktypeUrl: URI,
        omschrijving: String
    ) = Statustype(
        url = url,
        zaaktype = zaaktypeUrl,
        omschrijving = omschrijving,
        omschrijvingGeneriek = null,
        statustekst = null,
        volgnummer = 0,
        isEindstatus = null,
        informeren = null
    )

    private fun documentId() = "c3cafc6a-577c-4bd5-9dcb-c546c2b51a0e"
    private fun besluitTypeUrl(id: String = "1") = "https://example.com/besluittype/$id"
    private fun eigenschapUrl(id: String = "1") = "https://example.com/eigenschap/$id"
    private fun informatieObjectTypeUrl(id: String) = "https://example.com/informatieobjecttype/$id"
    private fun resultaatTypeUrl(id: String = "1") = "https://example.com/resultaattype/$id"
    private fun resultaatTypeOmschrijvingUrl(id: String = "1") = "https://example.com/resultaattypeomschrijving/$id"
    private fun selectielijstKlasseUrl(id: String = "1") = "https://example.com/selectielijstklasse/$id"
    private fun statustypeUrl(id: String = "1") = "https://example.com/statustype/$id"
    private fun zaaktypeUrl(id: String = "1") = "https://example.com/zaaktype/$id"

    private fun String.toURI() = URI(this)
    private fun String.toUUID() = UUID.fromString(this)
}

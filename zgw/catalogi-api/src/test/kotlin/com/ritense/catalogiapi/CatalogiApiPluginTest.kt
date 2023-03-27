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

package com.ritense.catalogiapi

import com.ritense.catalogiapi.client.CatalogiApiClient
import com.ritense.catalogiapi.client.ZaaktypeInformatieobjecttypeRequest
import com.ritense.catalogiapi.domain.Besluittype
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Resultaattype
import com.ritense.catalogiapi.domain.Statustype
import com.ritense.catalogiapi.domain.ZaaktypeInformatieobjecttype
import com.ritense.catalogiapi.exception.StatustypeNotFoundException
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.DocumentService
import com.ritense.zgw.Page
import org.camunda.community.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

internal class CatalogiApiPluginTest {

    val client = mock<CatalogiApiClient>()
    val zaaktypeUrlProvider = mock<ZaaktypeUrlProvider>()
    val documentService = mock<DocumentService>()
    val plugin = CatalogiApiPlugin(client, zaaktypeUrlProvider, documentService)

    @BeforeEach
    fun setUp() {
        plugin.authenticationPluginConfiguration = mock()
        plugin.url = mock()
    }

    @Test
    fun `should call client to get informatieobjecttypes`() {
        val zaakTypeUrl = URI("https://example.com/zaaktype")
        val resultPage = mock<Page<ZaaktypeInformatieobjecttype>>()
        whenever(
            client.getZaaktypeInformatieobjecttypes(
                plugin.authenticationPluginConfiguration,
                plugin.url,
                ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                )
            )
        ).thenReturn(resultPage)

        val mockZaaktypeInformatieobjecttype1 = mock<ZaaktypeInformatieobjecttype>()
        val mockZaaktypeInformatieobjecttype2 = mock<ZaaktypeInformatieobjecttype>()
        whenever(resultPage.results).thenReturn(
            listOf(
                mockZaaktypeInformatieobjecttype1,
                mockZaaktypeInformatieobjecttype2
            )
        )

        val mockInformatieobjecttype1 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl1 = URI("https://example.com/informatieobjecttype/1")
        whenever(mockZaaktypeInformatieobjecttype1.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl1)
        val mockInformatieobjecttype2 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl2 = URI("https://example.com/informatieobjecttype/2")
        whenever(mockZaaktypeInformatieobjecttype2.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl2)

        whenever(
            client.getInformatieobjecttype(
                plugin.authenticationPluginConfiguration,
                plugin.url,
                mockInformatieobjecttypeUrl1
            )
        ).thenReturn(mockInformatieobjecttype1)

        whenever(
            client.getInformatieobjecttype(
                plugin.authenticationPluginConfiguration,
                plugin.url,
                mockInformatieobjecttypeUrl2
            )
        ).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(2, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype1, informatieobjecttypes[0])
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[1])
    }

    @Test
    fun `should call client to get informatieobjecttypes with multiple pages`() {
        val zaakTypeUrl = URI("https://example.com/zaaktype")
        val resultPage1 = mock<Page<ZaaktypeInformatieobjecttype>>()
        whenever(resultPage1.next).thenReturn(URI("https://example.com/zaaktype/2"))
        val resultPage2 = mock<Page<ZaaktypeInformatieobjecttype>>()

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                plugin.authenticationPluginConfiguration,
                plugin.url,
                ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 1
                )
            )
        ).thenReturn(resultPage1)

        whenever(
            client.getZaaktypeInformatieobjecttypes(
                plugin.authenticationPluginConfiguration,
                plugin.url,
                ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = 2
                )
            )
        ).thenReturn(resultPage2)

        val mockZaaktypeInformatieobjecttype1 = mock<ZaaktypeInformatieobjecttype>()
        val mockZaaktypeInformatieobjecttype2 = mock<ZaaktypeInformatieobjecttype>()
        whenever(resultPage1.results).thenReturn(listOf(mockZaaktypeInformatieobjecttype1))
        whenever(resultPage2.results).thenReturn(listOf(mockZaaktypeInformatieobjecttype2))

        val mockInformatieobjecttype1 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl1 = URI("https://example.com/informatieobjecttype/1")
        whenever(mockZaaktypeInformatieobjecttype1.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl1)
        val mockInformatieobjecttype2 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl2 = URI("https://example.com/informatieobjecttype/2")
        whenever(mockZaaktypeInformatieobjecttype2.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl2)

        whenever(
            client.getInformatieobjecttype(
                plugin.authenticationPluginConfiguration,
                plugin.url,
                mockInformatieobjecttypeUrl1
            )
        ).thenReturn(mockInformatieobjecttype1)

        whenever(
            client.getInformatieobjecttype(
                plugin.authenticationPluginConfiguration,
                plugin.url,
                mockInformatieobjecttypeUrl2
            )
        ).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(2, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype1, informatieobjecttypes[0])
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[1])
    }

    @Test
    fun `should get status type`() {
        val documentId = UUID.randomUUID().toString()
        val document = mock<Document>()
        val statustype = "Registered"
        val statustypeUrl = "https://example.com/statustype/456"
        val zaaktypeUrl = "https://example.com/zaaktype/123"
        val execution = DelegateExecutionFake().withBusinessKey(documentId)
        whenever(document.definitionId()).thenReturn(JsonSchemaDocumentDefinitionId.newId("myDocDef"))
        whenever(documentService.get(documentId)).thenReturn(document)
        whenever(zaaktypeUrlProvider.getZaaktypeUrl("myDocDef")).thenReturn(URI(zaaktypeUrl))
        whenever(client.getStatustypen(any(), any(), any())).thenReturn(
            Page(
                count = 3,
                results = listOf(
                    Statustype(URI("example.com/1"), URI(zaaktypeUrl), "other status", null, null, 0, null, null),
                    Statustype(URI(statustypeUrl), URI(zaaktypeUrl), statustype, null, null, 0, null, null),
                    Statustype(URI("example.com/2"), URI(zaaktypeUrl), "yet another status", null, null, 0, null, null),
                )
            )
        )
        plugin.getStatustype(
            execution, statustype, "myProcessVar"
        )

        assertEquals(statustypeUrl, execution.getVariable("myProcessVar"))
    }

    @Test
    fun `should throw StatustypeNotFoundException when get status type doesn't exist`() {
        val documentId = UUID.randomUUID().toString()
        val document = mock<Document>()
        val statustype = "Registered"
        val zaaktypeUrl = "https://example.com/zaaktype/123"
        val execution = DelegateExecutionFake().withBusinessKey(documentId)
        whenever(document.definitionId()).thenReturn(JsonSchemaDocumentDefinitionId.newId("myDocDef"))
        whenever(documentService.get(documentId)).thenReturn(document)
        whenever(zaaktypeUrlProvider.getZaaktypeUrl("myDocDef")).thenReturn(URI(zaaktypeUrl))
        whenever(client.getStatustypen(any(), any(), any())).thenReturn(
            Page(count = 0, results = listOf())
        )

        val exception = assertThrows<StatustypeNotFoundException> {
            plugin.getStatustype(
                execution, statustype, "myProcessVar"
            )
        }

        assertEquals("No statustype was found. With 'omschrijving': 'Registered'", exception.message)
    }

    @Test
    fun `should get resultaat type`() {
        val exampleUrl = URI("example.com")
        val documentId = UUID.randomUUID().toString()
        val document = mock<Document>()
        val resultaattype = "Registered"
        val resultaattypeUrl = "https://example.com/resultaattype/456"
        val zaaktypeUrl = "https://example.com/zaaktype/123"
        val execution = DelegateExecutionFake().withBusinessKey(documentId)
        whenever(document.definitionId()).thenReturn(JsonSchemaDocumentDefinitionId.newId("myDocDef"))
        whenever(documentService.get(documentId)).thenReturn(document)
        whenever(zaaktypeUrlProvider.getZaaktypeUrl("myDocDef")).thenReturn(URI(zaaktypeUrl))
        whenever(client.getResultaattypen(any(), any(), any())).thenReturn(
            Page(
                count = 3,
                results = listOf(
                    Resultaattype(
                        URI("example.com/1"),
                        URI(zaaktypeUrl),
                        "other resultaat",
                        exampleUrl,
                        null,
                        exampleUrl,
                        null
                    ),
                    Resultaattype(
                        URI(resultaattypeUrl),
                        URI(zaaktypeUrl),
                        resultaattype,
                        exampleUrl,
                        null,
                        exampleUrl,
                        null
                    ),
                    Resultaattype(
                        URI("example.com/2"),
                        URI(zaaktypeUrl),
                        "yet another resultaat",
                        exampleUrl,
                        null,
                        exampleUrl,
                        null
                    ),
                )
            )
        )
        plugin.getResultaattype(
            execution, resultaattype, "myProcessVar"
        )

        assertEquals(resultaattypeUrl, execution.getVariable("myProcessVar"))
    }

    @Test
    fun `should get besluit type`() {
        val exampleUrl = URI("example.com")
        val documentId = UUID.randomUUID().toString()
        val document = mock<Document>()
        val besluittype = "Allocated"
        val besluittypeUrl = "https://example.com/besluittype/456"
        val zaaktypeUrl = "https://example.com/zaaktype/123"
        val execution = DelegateExecutionFake().withBusinessKey(documentId)
        whenever(document.definitionId()).thenReturn(JsonSchemaDocumentDefinitionId.newId("myDocDef"))
        whenever(documentService.get(documentId)).thenReturn(document)
        whenever(zaaktypeUrlProvider.getZaaktypeUrl("myDocDef")).thenReturn(URI(zaaktypeUrl))
        whenever(client.getBesluittypen(any(), any(), any())).thenReturn(
            Page(
                count = 3,
                results = listOf(
                    Besluittype(
                        URI("example.com/1"),
                        URI(zaaktypeUrl),
                        listOf(),
                        "other besluit",
                        null,
                        null,
                        null,
                        true,
                        null,
                        null,
                        null,
                        listOf(),
                        LocalDate.now(),
                        null,
                        null
                    ),
                    Besluittype(
                        URI(besluittypeUrl),
                        URI(zaaktypeUrl),
                        listOf(),
                        besluittype,
                        null,
                        null,
                        null,
                        true,
                        null,
                        null,
                        null,
                        listOf(),
                        LocalDate.now(),
                        null,
                        null
                    ),
                    Besluittype(
                        URI("example.com/2"),
                        URI(zaaktypeUrl),
                        listOf(),
                        "yet another besluit",
                        null,
                        null,
                        null,
                        true,
                        null,
                        null,
                        null,
                        listOf(),
                        LocalDate.now(),
                        null,
                        null
                    ),
                )
            )
        )
        plugin.getBesluittype(
            execution, besluittype, "myProcessVar"
        )

        assertEquals(besluittypeUrl, execution.getVariable("myProcessVar"))
    }

    @Test
    fun `should get besluit type by url`() {
        val documentId = UUID.randomUUID().toString()
        val besluittype = "http://example.com/besluittype/456"
        val execution = DelegateExecutionFake().withBusinessKey(documentId)

        plugin.getBesluittype(execution, besluittype, "myProcessVar")

        assertEquals(besluittype, execution.getVariable("myProcessVar"))
    }

}

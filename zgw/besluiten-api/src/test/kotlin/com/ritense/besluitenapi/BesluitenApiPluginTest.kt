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

package com.ritense.besluitenapi

import com.ritense.besluitenapi.client.BesluitenApiClient
import com.ritense.besluitenapi.domain.BesluitInformatieObject
import com.ritense.besluitenapi.domain.CreateBesluitInformatieObject
import com.ritense.zgw.Rsin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.*
import org.springframework.http.ResponseEntity
import java.net.URI
import java.util.UUID

class BesluitenApiPluginTest {

    lateinit var besluitenApiPlugin: BesluitenApiPlugin
    lateinit var besluitenApiClient: BesluitenApiClient

    @BeforeEach
    fun init() {
        besluitenApiClient = mock()
        besluitenApiPlugin = BesluitenApiPlugin(besluitenApiClient)
        besluitenApiPlugin.authenticationPluginConfiguration = mock()
        besluitenApiPlugin.url = URI.create("https://some-host.nl/besluiten/api/v1/besluitinformatieobjecten")
        besluitenApiPlugin.rsin = Rsin("252170362")
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
            ResponseEntity.ok().body(BesluitInformatieObject(besluitInformatieObjectUrl, documentUrl, besluitUrl))
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
        assertEquals(besluitInformatieObjectValue.informatieobject,documentUrl)
        assertEquals(besluitenApiAuthenticationValue, besluitenApiPlugin.authenticationPluginConfiguration)
        assertEquals(uriValue, besluitenApiPlugin.url)
    }

}
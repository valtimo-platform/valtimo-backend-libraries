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
        assertEquals(besluitInformatieObjectValue.informatieobject,documentUrl)
        assertEquals(besluitenApiAuthenticationValue, besluitenApiPlugin.authenticationPluginConfiguration)
        assertEquals(uriValue, besluitenApiPlugin.url)
    }

}
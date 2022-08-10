package com.ritense.zakenapi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.service.DocumentService
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import kotlin.test.assertEquals

internal class ZakenApiPluginTest {

    @Test
    fun `should link document to zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val resourceProvider: ResourceProvider = mock()
        val documentService: DocumentService = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        whenever(executionMock.getVariable("businessKey")).thenReturn("5520c12a-20a3-4f45-8ccf-b19a95e4edcd")
        whenever(zaakUrlProvider.getZaak(any())).thenReturn("http://zaak.url")

        val plugin = ZakenApiPlugin(zakenApiClient, zaakUrlProvider, resourceProvider, documentService)
        plugin.url = "http://zaken.plugin.url"
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.linkDocumentToZaak(executionMock, "http://document.url", "titel", "beschrijving")

        val captor = argumentCaptor<LinkDocumentRequest>()
        verify(zakenApiClient).linkDocument(any(), anyString(), captor.capture())
        verify(documentService).assignResource(any(), any());

        val request = captor.firstValue
        assertEquals("http://document.url", request.informatieobject)
        assertEquals("http://zaak.url", request.zaak)
        assertEquals("titel", request.titel)
        assertEquals("beschrijving", request.beschrijving)
    }
}
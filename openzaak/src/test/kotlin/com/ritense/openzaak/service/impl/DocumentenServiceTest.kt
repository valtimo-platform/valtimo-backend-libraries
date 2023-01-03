package com.ritense.openzaak.service.impl

import org.mockito.kotlin.verify
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Arrays
import java.util.UUID

internal class DocumentenServiceTest {

    val restTemplate = mock(RestTemplate::class.java)
    val openZaakConfigService = mock(OpenZaakConfigService::class.java)
    val openZaakTokenGeneratorService = mock(OpenZaakTokenGeneratorService::class.java)
    val informatieObjectTypeLinkService = mock(InformatieObjectTypeLinkService::class.java)
    val zaakInstanceLinkService = mock(ZaakInstanceLinkService::class.java)

    val service = DocumentenService(
        restTemplate,
        openZaakConfigService,
        openZaakTokenGeneratorService,
        informatieObjectTypeLinkService,
        zaakInstanceLinkService
    )

    @Test
    fun `getObjectInformatieObject performs request to documenten API`() {
        val documentId = UUID.randomUUID()
        val documentUri = URI("http://some.url/documenten/api/v1/enkelvoudiginformatieobjecten/$documentId")

        val urlCaptor = ArgumentCaptor.forClass(String::class.java)
        val methodCaptor = ArgumentCaptor.forClass(HttpMethod::class.java)
        val bodyCaptor = ArgumentCaptor.forClass(HttpEntity::class.java)

        `when`(restTemplate.exchange(
            MockitoHelper.anyObject<String>(),
            MockitoHelper.anyObject(),
            MockitoHelper.anyObject(),
            MockitoHelper.anyObject<ParameterizedTypeReference<ByteArray>>()
        )).thenReturn(ResponseEntity.ok("content".toByteArray()))

        `when`(openZaakConfigService.getOpenZaakConfig()).thenReturn(OpenZaakConfig(
            "http://documenten.api",
            "client",
            "secret",
            Rsin("051845623")
        ))

        `when`(openZaakTokenGeneratorService.generateToken("secret", "client"))
            .thenReturn("some-token")

        val bytes = service.getObjectInformatieObject(documentUri)

        verify(restTemplate).exchange(
            urlCaptor.capture(),
            methodCaptor.capture(),
            bodyCaptor.capture(),
            MockitoHelper.anyObject<ParameterizedTypeReference<ByteArray>>()
        )

        assertEquals("http://documenten.api/documenten/api/v1/enkelvoudiginformatieobjecten/$documentId/download", urlCaptor.value)
        assertEquals(HttpMethod.GET, methodCaptor.value)
        assertTrue(Arrays.equals("content".toByteArray(), bytes))

        val httpEntity = bodyCaptor.value
        assertTrue(httpEntity.headers.accept.contains(MediaType.APPLICATION_OCTET_STREAM))
    }

    object MockitoHelper {
        fun <T> anyObject(): T {
            Mockito.any<T>()
            return uninitialized()
        }
        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(): T =  null as T
    }
}
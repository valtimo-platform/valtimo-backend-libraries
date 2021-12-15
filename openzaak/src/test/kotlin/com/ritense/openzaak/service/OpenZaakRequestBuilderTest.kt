/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.service.impl.OpenZaakRequestBuilder
import com.ritense.openzaak.service.impl.model.ResultWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoAnnotations
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI
import kotlin.test.assertTrue
import org.springframework.http.MediaType

class OpenZaakRequestBuilderTest : BaseTest() {

    lateinit var openZaakRequestBuilder: OpenZaakRequestBuilder

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        baseSetUp()
        openZaakRequestBuilder = OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
    }

    @Test
    fun `should build and execute and throw exception`() {
        //given
        openZaakRequestBuilder
            .path("aPath")
            .build()

        http500(openZaakRequestBuilder)

        //when and then
        assertThrows(IllegalStateException::class.java) {
            openZaakRequestBuilder.execute(Map::class.java)
        }
    }

    @Test
    fun `should build and execute with response mapped`() {
        //given
        openZaakRequestBuilder
            .path("aPath")
            .build()

        httpOk(openZaakRequestBuilder)

        //when
        val result = openZaakRequestBuilder.execute(Map::class.java)

        //then
        assertThat(result).isNotNull
        assertThat(result["url"]).isEqualTo("http://example.com")
    }

    @Test
    fun `should build and execute with response wrapped mapped`() {
        val builder = openZaakRequestBuilder
            .path("aPath")
            .build()

        httpOkWrapped(builder)

        val result = builder.executeWrapped(Map::class.java)

        assertThat(result).isNotNull
        assertThat(result).hasFieldOrProperty("count")
        assertThat(result).hasFieldOrProperty("next")
        assertThat(result).hasFieldOrProperty("previous")
        assertThat(result).hasFieldOrProperty("results")
        assertThat(result.results).hasSize(1)
        assertThat(result.results.first()["url"]).isEqualTo("http://example.com")
    }

    @Test
    fun `should build and execute with response mapped for collection`() {
        val builder = openZaakRequestBuilder
            .path("aPath")
            .build()

        httpOkForCollection(builder)

        val result = builder.executeForCollection(Map::class.java)

        assertThat(result).isNotNull
        assertThat(result).hasSize(1)
        assertThat(result.first()["url"]).isEqualTo("http://example.com")
    }

    @Test
    fun `should add content type to request`() {
        //when
        val builder = openZaakRequestBuilder
            .path("aPath")
            .acceptHeader(listOf(MediaType.TEXT_PLAIN))
            .build()

        assertTrue(builder.requestEntity.headers.accept.contains(MediaType.TEXT_PLAIN))
    }


    private fun http500(builder: OpenZaakRequestBuilder) {
        val responseEntity: ResponseEntity<Any> = ResponseEntity(
            null,
            httpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
        whenever(restTemplate.exchange(
            eq(builder.url),
            eq(builder.method),
            eq(builder.requestEntity),
            any(ParameterizedTypeReference::class.java)
        )).thenReturn(responseEntity)
    }

    private fun httpOk(builder: OpenZaakRequestBuilder) {
        val responseEntity = ResponseEntity(
            mapOf("url" to "http://example.com"),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(restTemplate.exchange(
            eq(builder.url),
            eq(builder.method),
            eq(builder.requestEntity),
            any(ParameterizedTypeReference::class.java)
        )).thenReturn(responseEntity)
    }

    private fun httpOkWrapped(builder: OpenZaakRequestBuilder) {
        val responseEntity = ResponseEntity(
            ResultWrapper(
                1,
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                listOf(mapOf("url" to "http://example.com"))
            ),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(restTemplate.exchange(
            eq(builder.url),
            eq(builder.method),
            eq(builder.requestEntity),
            any(ParameterizedTypeReference::class.java)
        )).thenReturn(responseEntity)
    }

    private fun httpOkForCollection(builder: OpenZaakRequestBuilder) {
        val responseEntity = ResponseEntity(
            listOf(mapOf("url" to "http://example.com")),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(restTemplate.exchange(
            eq(builder.url),
            eq(builder.method),
            eq(builder.requestEntity),
            any(ParameterizedTypeReference::class.java)
        )).thenReturn(responseEntity)
    }

}
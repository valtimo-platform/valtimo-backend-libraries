/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.openzaak.catalogi

import com.nhaarman.mockitokotlin2.whenever
import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.BesluitType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

class CatalogiClientTest : BaseTest() {

    lateinit var catalogiClient: CatalogiClient

    @BeforeEach
    fun setUp() {
        baseSetUp()
        catalogiClient = CatalogiClient(
            restTemplate,
            openZaakConfigService,
            openZaakTokenGeneratorService
        )
    }

    @Test
    fun `should get besluit typen`() {
        //given
        httpCall(
            listOf(
                BesluitType(
                    URI.create("http://example.com"),
                    "omschrijving"
                )
            )
        )
        val result = catalogiClient.getBesluittypen()

        //when
        val zaaktype = result.results.first()

        //then
        assertThat(zaaktype.url).isEqualTo(URI.create("http://example.com"))
        assertThat(zaaktype.omschrijving).isEqualTo("omschrijving")
    }

    private fun httpCall(entities: List<Any>) {
        val responseEntity = ResponseEntity(
            ResultWrapper(
                1,
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                entities
            ),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(
            restTemplate.exchange(
                anyString(),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                any(ParameterizedTypeReference::class.java)
            )
        ).thenReturn(responseEntity)
    }

}
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

import org.mockito.kotlin.whenever
import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.service.impl.ZaakResultaatService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.ResultaatType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

class ZaakResultaatServiceTest : BaseTest() {

    lateinit var zaakresultaatService: ZaakResultaatService

    @BeforeEach
    fun setup() {
        baseSetUp()
        zaakresultaatService = ZaakResultaatService(
            restTemplate,
            openZaakConfigService,
            openZaakTokenGeneratorService
        )
        httpGetZaakResultaatTypes()
    }

    @Test
    fun `should get resultaattypen of a certain zaaktype`() {
        val zaakresultaattypen = zaakresultaatService.getResultaatTypes(URI("http://example.com"))

        val zaakresultaattype = zaakresultaattypen.results.first()

        assertThat(zaakresultaattype.url).isEqualTo(URI("http://example.com"))
        assertThat(zaakresultaattype.zaaktype).isEqualTo(URI("http://example.com"))
        assertThat(zaakresultaattype.omschrijving).isEqualTo("aOmschrijving")
        assertThat(zaakresultaattype.resultaattypeomschrijving).isEqualTo(URI("http://example.com"))
    }

    private fun httpGetZaakResultaatTypes() {
        val responseEntity = ResponseEntity(
            ResultWrapper(
                1,
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                listOf(ResultaatType(
                    URI.create("http://example.com"),
                    URI.create("http://example.com"),
                    "aOmschrijving",
                    URI.create("http://example.com"),
                    selectielijstklasse = URI.create("http://example.com")
                ))
            ),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(HttpMethod::class.java),
            ArgumentMatchers.any(HttpEntity::class.java),
            ArgumentMatchers.any(ParameterizedTypeReference::class.java)
        )).thenReturn(responseEntity)
    }

}
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
import com.ritense.openzaak.service.impl.ZaakStatusService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.StatusType
import java.net.URI
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

class ZaakStatusServiceTest : BaseTest() {

    lateinit var zaakStatusService: ZaakStatusService

    @BeforeEach
    fun setUp() {
        baseSetUp()
        zaakStatusService = ZaakStatusService(
            restTemplate,
            openZaakConfigService,
            openZaakTokenGeneratorService,
            documentService,
            zaakTypeLinkService,
            zaakInstanceLinkService
        )
    }

    @Test
    fun `should get zaakstatussen`() {
        //given
        val zaaktype = URI("http://example.com")
        httpGetZaakstatusTypes()

        //when
        val result = zaakStatusService.getStatusTypes(zaaktype)

        //then
        val zaakstatus = result.results.first()
        assertThat(zaakstatus.url).isEqualTo(URI("http://example.com"))
        assertThat(zaakstatus.omschrijving).isEqualTo("aOmschrijving")
        assertThat(zaakstatus.omschrijvingGeneriek).isEqualTo("aOmschrijvingGeneriek")
        assertThat(zaakstatus.statustekst).isEqualTo("aStatusTekst")
        assertThat(zaakstatus.zaaktype).isEqualTo(URI("http://example.com"))
        assertThat(zaakstatus.volgnummer).isEqualTo(1)
        assertThat(zaakstatus.isEindstatus).isEqualTo(true)
        assertThat(zaakstatus.informeren).isEqualTo(true)
    }

    @Test
    fun `should get zaak status type by url`() {
        //given
        val statusTypeUrl = URI("http://localhost:8001/catalogi/api/v1/statustypen/0641e9db-7eac-47b6-b60d-d741ed2b5c16")
        httpGetZaakstatusType()

        //when
        val zaakstatus = zaakStatusService.getStatusType(statusTypeUrl)!!

        //then
        assertThat(zaakstatus.url).isEqualTo(URI("http://example.com"))
        assertThat(zaakstatus.omschrijving).isEqualTo("aOmschrijving")
        assertThat(zaakstatus.omschrijvingGeneriek).isEqualTo("aOmschrijvingGeneriek")
        assertThat(zaakstatus.statustekst).isEqualTo("aStatusTekst")
        assertThat(zaakstatus.zaaktype).isEqualTo(URI("http://example.com"))
        assertThat(zaakstatus.volgnummer).isEqualTo(1)
        assertThat(zaakstatus.isEindstatus).isEqualTo(true)
        assertThat(zaakstatus.informeren).isEqualTo(true)
    }

    private fun httpGetZaakstatusTypes() {
        val responseEntity = ResponseEntity(
            ResultWrapper(
                1,
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                listOf(exampleStatusType())
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

    private fun httpGetZaakstatusType() {
        val responseEntity = ResponseEntity(
            exampleStatusType(),
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

    private fun exampleStatusType(): StatusType {
        return StatusType(
            URI.create("http://example.com"),
            "aOmschrijving",
            "aOmschrijvingGeneriek",
            "aStatusTekst",
            URI.create("http://example.com"),
            1,
            isEindstatus = true,
            informeren = true
        )
    }

}

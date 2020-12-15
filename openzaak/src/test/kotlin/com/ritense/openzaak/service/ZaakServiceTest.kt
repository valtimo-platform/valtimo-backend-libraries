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
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinks
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.model.zaak.Zaak
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
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
import java.util.UUID

class ZaakServiceTest : BaseTest() {

    private val UUID_STRING = "91e750e1-53ab-4922-9979-6a2dacd009cf"

    val zaaktypeLinkId = ZaakTypeLinkId.existingId(UUID.randomUUID())

    val zaakType = URI.create("http://example.com")

    @BeforeEach
    fun setUp() {
        baseSetUp()
        whenever(zaakTypeLinkService.findBy(document.definitionId().name())).thenReturn(
            ZaakTypeLink(
                zaaktypeLinkId,
                "house",
                zaakType,
                ZaakInstanceLinks(),
                ServiceTaskHandlers()
            )
        )
        httpZaakCreated()
        zaakService = ZaakService(
            restTemplate,
            openZaakConfigService,
            openZaakTokenGeneratorService,
            zaakTypeLinkService,
            documentService
        )
    }

    @Test
    fun `should create zaak with link`() {
        //given
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessBusinessKey(document.id!!.id.toString())

        //when
        zaakService.createZaakWithLink(delegateExecutionFake)

        //then
        verify(zaakTypeLinkService).assignZaakInstance(
            eq(zaaktypeLinkId),
            eq(ZaakInstanceLink(URI.create("http://example.com"), UUID.fromString(UUID_STRING), document.id!!.id))
        )
    }

    private fun httpZaakCreated() {
        val responseEntity = ResponseEntity(
            Zaak(
                URI.create("http://example.com"),
                UUID.fromString(UUID_STRING),
                "",
                URI.create("http://example.com"),
                "",
                "",
                listOf(URI.create("http://example.com"))
            ),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(restTemplate.exchange(
            anyString(),
            any(HttpMethod::class.java),
            any(HttpEntity::class.java),
            any(ParameterizedTypeReference::class.java)
        )).thenReturn(responseEntity)
    }

}

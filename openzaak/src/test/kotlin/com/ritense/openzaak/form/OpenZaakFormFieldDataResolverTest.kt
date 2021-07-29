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

package com.ritense.openzaak.form

import com.nhaarman.mockitokotlin2.whenever
import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinks
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.model.zaak.Eigenschap
import com.ritense.openzaak.service.impl.model.zaak.Zaak
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
import java.util.UUID

internal class OpenZaakFormFieldDataResolverTest : BaseTest() {

    val zaaktypeLinkId = ZaakTypeLinkId.existingId(UUID.randomUUID())

    lateinit var openZaakFormFieldDataResolver: OpenZaakFormFieldDataResolver

    val zaakType = URI.create("http://example.com")

    @BeforeEach
    fun setUp() {
        baseSetUp()
        val zaakTypeLink = ZaakTypeLink(
            zaaktypeLinkId,
            "house",
            zaakType,
            ZaakInstanceLinks(),
            ServiceTaskHandlers()
        )
        zaakTypeLink.assignZaakInstance(
            ZaakInstanceLink(
                URI.create("http://example.com"),
                UUID.randomUUID(),
                document.id.id
            )
        )

        whenever(zaakTypeLinkService.findBy(document.definitionId().name())).thenReturn(
            zaakTypeLink
        )
        httpZaakCreated()
        httpGetZaakEigenschappen()
        zaakService = ZaakService(
            restTemplate,
            openZaakConfigService,
            openZaakTokenGeneratorService,
            zaakTypeLinkService,
            documentService
        )
        openZaakFormFieldDataResolver = OpenZaakFormFieldDataResolver(zaakService, zaakTypeLinkService)
    }

    @Test
    fun `should not contain eigenschap with unknown key`() {
        val resultMap = openZaakFormFieldDataResolver.get("house", document.id.id, "unknownVarName")
        assertThat(resultMap).isNotNull
        assertThat(resultMap).doesNotContainKey("unknownVarName")
    }

    @Test
    fun `should get open zaak eigenschappen value`() {
        val resultMap = openZaakFormFieldDataResolver.get("house", document.id.id, "varNaam")
        assertThat(resultMap).isNotNull
        assertThat(resultMap).containsEntry("varNaam", "varWaarde")
    }

    private fun httpZaakCreated() {
        val responseEntity = ResponseEntity(
            Zaak(
                URI.create("http://example.com"),
                UUID.fromString("91e750e1-53ab-4922-9979-6a2dacd009cf"),
                "",
                "",
                "",
                "",
                URI.create("http://example.com"),
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                listOf(URI.create("http://example.com")),
                "",
                "",
                "",
                "",
                Zaak.Zaakgeometrie("", listOf(1)),
                Zaak.Verlenging("",""),
                Zaak.Opschorting(true,""),
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                listOf(URI.create("http://example.com")),
                listOf(Zaak.RelevanteAndereZaken(URI.create("http://example.com"),"")),
                listOf(URI.create("http://example.com")),
                URI.create("http://example.com"),
                "",
                listOf(Zaak.Kenmerken("","")),
                "",
                "",
                "",
                URI.create("http://example.com"),
                ""
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

    private fun httpGetZaakEigenschappen() {
        val responseEntity = ResponseEntity(
            listOf(Eigenschap(
                URI.create("http://example.com"),
                UUID.randomUUID(),
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                "varNaam",
                "varWaarde"
            )),
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
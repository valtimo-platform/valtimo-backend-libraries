/*
 *
 *  * Copyright 2015-2021 Ritense BV, the Netherlands.
 *  *
 *  * Licensed under EUPL, Version 1.2 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.ritense.openzaak.service

import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinkId
import com.ritense.openzaak.exception.ZaakInstanceLinkNotFoundException
import com.ritense.openzaak.repository.ZaakInstanceLinkRepository
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import java.net.URI
import java.util.Optional
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ZaakInstanceLinkServiceTest: BaseTest() {

    lateinit var zaakInstanceLink: ZaakInstanceLink
    lateinit var zaakInstanceLinkId: ZaakInstanceLinkId

    @Mock
    lateinit var zaakInstanceLinkRepository: ZaakInstanceLinkRepository

    val zaakInstanceUrl = URI.create("http://zaak.instanceUrl.nl")
    val zaakTypeUrl = URI.create("http://zaak.typeUrl.nl")
    val documentId = UUID.randomUUID()
    val zaakInstanceId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        zaakInstanceLinkService = ZaakInstanceLinkService(zaakInstanceLinkRepository)
        zaakInstanceLinkId = ZaakInstanceLinkId.newId(UUID.randomUUID())

        zaakInstanceLink = ZaakInstanceLink(
            zaakInstanceLinkId,
            zaakInstanceUrl,
            zaakInstanceId,
            documentId,
            zaakTypeUrl
        )

        whenever(zaakInstanceLinkRepository.findById(zaakInstanceLinkId)).thenReturn(Optional.of(zaakInstanceLink))
        whenever(zaakInstanceLinkRepository.findByDocumentId(documentId)).thenReturn(zaakInstanceLink)
        whenever(zaakInstanceLinkRepository.save(any(ZaakInstanceLink::class.java))).thenReturn(zaakInstanceLink)
    }


    @Test
    fun `should create entity`() {
        val result = zaakInstanceLinkService.createZaakInstanceLink(
            zaakInstanceUrl,
            zaakInstanceId,
            documentId,
            zaakTypeUrl
        )

        assertThat(result).isNotNull
        assertThat(result.id.isNew)
        assertThat(result.zaakInstanceLinkId.id).isEqualTo(zaakInstanceLinkId.id)
        assertThat(result.zaakInstanceUrl).isEqualTo(zaakInstanceUrl)
        assertThat(result.zaakInstanceId).isEqualTo(zaakInstanceId)
        assertThat(result.documentId).isEqualTo(documentId)
        assertThat(result.zaakTypeUrl).isEqualTo(zaakTypeUrl)
    }

    @Test
    fun `should get entity by document id`() {
        val result = zaakInstanceLinkService.getByDocumentId(documentId)

        assertThat(result).isNotNull
        assertThat(!result.id.isNew)
        assertThat(result.zaakInstanceLinkId.id).isEqualTo(zaakInstanceLinkId.id)
        assertThat(result.zaakInstanceUrl).isEqualTo(zaakInstanceUrl)
        assertThat(result.zaakInstanceId).isEqualTo(zaakInstanceId)
        assertThat(result.documentId).isEqualTo(documentId)
        assertThat(result.zaakTypeUrl).isEqualTo(zaakTypeUrl)
    }

    @Test
    fun `should not find entity by invalid document id`() {
        assertThrows(ZaakInstanceLinkNotFoundException::class.java) {
            zaakInstanceLinkService.getByDocumentId(UUID.randomUUID())
        }
    }

    @Test
    fun `should get entity by zaak instance url`() {
        whenever(zaakInstanceLinkRepository.findByZaakInstanceUrl(zaakInstanceUrl)).thenReturn(zaakInstanceLink)

        val result = zaakInstanceLinkService.getByZaakInstanceUrl(zaakInstanceUrl)

        assertThat(result).isNotNull
        assertThat(!result.id.isNew)
        assertThat(result.zaakInstanceLinkId.id).isEqualTo(zaakInstanceLinkId.id)
        assertThat(result.zaakInstanceUrl).isEqualTo(zaakInstanceUrl)
        assertThat(result.zaakInstanceId).isEqualTo(zaakInstanceId)
        assertThat(result.documentId).isEqualTo(documentId)
        assertThat(result.zaakTypeUrl).isEqualTo(zaakTypeUrl)
    }

    @Test
    fun `should not find entity by invalid zaak instance url`() {
        assertThrows(ZaakInstanceLinkNotFoundException::class.java) {
            zaakInstanceLinkService.getByZaakInstanceUrl(URI.create("https://fake-url.com/"))
        }
    }
}
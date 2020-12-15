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

import com.ritense.openzaak.BaseIntegrationTest
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID
import javax.inject.Inject
import javax.transaction.Transactional

@Transactional
class ZaakLinkServiceIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    val zaakType = URI.create("test")

    @Test
    fun `should create zaaktypeLink`() {
        val result = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakType)
        )

        assertThat(result.zaakTypeLink()).isNotNull
        assertThat(result.zaakTypeLink()!!.documentDefinitionName).isEqualTo("test")
        assertThat(result.zaakTypeLink()!!.zaakTypeUrl).isEqualTo(zaakType)
    }

    @Test
    fun `should create zaaktypeLink and assign zaakInstance`() {
        val result = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakType)
        )

        val zaakInstanceLink = ZaakInstanceLink(URI.create("http://example.com"), UUID.randomUUID(), UUID.randomUUID())

        val zaaktypeLink = zaakTypeLinkService.assignZaakInstance(
            result.zaakTypeLink()!!.zaakTypeLinkId,
            zaakInstanceLink
        )

        assertThat(zaaktypeLink).isNotNull
        assertThat(zaaktypeLink.zaakInstanceLinks).contains(zaakInstanceLink)
    }

}
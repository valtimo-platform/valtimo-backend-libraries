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
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import java.net.URI
import javax.inject.Inject
import javax.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Transactional
class ZaakLinkServiceIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    val zaakType = URI.create("test")

    @Test
    fun `should create zaaktypeLink without createWithDossier`() {
        val result = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakType)
        )

        assertThat(result.zaakTypeLink()).isNotNull
        assertThat(result.zaakTypeLink()!!.documentDefinitionName).isEqualTo("test")
        assertThat(result.zaakTypeLink()!!.zaakTypeUrl).isEqualTo(zaakType)
        assertThat(result.zaakTypeLink()!!.createWithDossier).isEqualTo(false)
    }

    @Test
    fun `should create zaaktypeLink with createWithDossier`() {
        val result = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakType, true)
        )

        assertThat(result.zaakTypeLink()).isNotNull
        assertThat(result.zaakTypeLink()!!.documentDefinitionName).isEqualTo("test")
        assertThat(result.zaakTypeLink()!!.zaakTypeUrl).isEqualTo(zaakType)
        assertThat(result.zaakTypeLink()!!.createWithDossier).isEqualTo(true)
    }

}
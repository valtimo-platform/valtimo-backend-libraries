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

package com.ritense.zakenapi.service

import com.ritense.zakenapi.BaseIntegrationTest
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Transactional
class ZaakTypeLinkServiceIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    val zaakUrl = URI.create("http://example.com")

    @Test
    fun `should create zaakTypeLink`() {
        val result = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakUrl)
        )
        assertThat(result.documentDefinitionName).isEqualTo("test")
        assertThat(result.zaakTypeUrl).isEqualTo(zaakUrl)
    }
}
/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.provider

import com.ritense.catalogiapi.exception.ZaakTypeLinkNotFoundException
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.zakenapi.domain.ZaakTypeLink
import com.ritense.zakenapi.domain.ZaakTypeLinkId
import com.ritense.zakenapi.service.ZaakTypeLinkService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.UUID

class DefaultZaaktypeUrlProviderTest {

    lateinit var zaaktypeUrlProvider: DefaultZaaktypeUrlProvider

    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @BeforeEach
    fun setup() {
        zaakTypeLinkService = mock()
        zaaktypeUrlProvider = DefaultZaaktypeUrlProvider(zaakTypeLinkService)
    }

    @Test
    fun `should get zaaktype URL by case definition id`() {
        val caseDefinitionId = CaseDefinitionId("test", "1.0.0")

        val zaakTypeLink = createZaakTypeLink(caseDefinitionId)
        whenever(zaakTypeLinkService.get(caseDefinitionId)).thenReturn(zaakTypeLink)

        val zaaktypeUrl = zaaktypeUrlProvider.getZaaktypeUrl(caseDefinitionId)

        assertThat(zaaktypeUrl).isEqualTo(zaakTypeLink.zaakTypeUrl)
    }

    @Test
    fun `should throw ZaakTypeLinkNotFoundException on getZaaktypeUrl when ZaakTypeLink cannot be found`() {
        val caseDefinitionId = CaseDefinitionId("test", "1.0.0")

        whenever(zaakTypeLinkService.get(caseDefinitionId)).thenReturn(null)

        val ex = assertThrows<ZaakTypeLinkNotFoundException> {
            zaaktypeUrlProvider.getZaaktypeUrl(caseDefinitionId)
        }

        assertThat(ex.message).endsWith("For case definition $caseDefinitionId")
    }

    private fun createZaakTypeLink(caseDefinitionId: CaseDefinitionId) = ZaakTypeLink(
        ZaakTypeLinkId.newId(UUID.randomUUID()),
        caseDefinitionId,
        URI("http://localhost/${caseDefinitionId.key}")
    )
}
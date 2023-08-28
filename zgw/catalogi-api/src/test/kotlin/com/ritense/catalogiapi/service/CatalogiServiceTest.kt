/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.catalogiapi.service

import com.ritense.catalogiapi.CatalogiApiPlugin
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI

internal class CatalogiServiceTest {

    val zaaktypeUrlProvider = mock<ZaaktypeUrlProvider>()
    val pluginService = mock<PluginService>()
    val catalogiService = CatalogiService(zaaktypeUrlProvider, pluginService)

    @Test
    fun `should get informatieobjecttypes for document definition`() {
        val documentDefinitionName = "case-name"
        val zaaktypeUrl = URI("http://example.com/zaaktype")

        whenever(zaaktypeUrlProvider.getZaaktypeUrl(documentDefinitionName)).thenReturn(zaaktypeUrl)

        val catalogiApiPlugin = mock<CatalogiApiPlugin>()
        whenever(pluginService.createInstance(eq(CatalogiApiPlugin::class.java), any()))
            .thenReturn(catalogiApiPlugin)

        val informatieobjecttype1 = mock<Informatieobjecttype>()
        val informatieobjecttype2 = mock<Informatieobjecttype>()
        whenever(catalogiApiPlugin.getInformatieobjecttypes(zaaktypeUrl))
            .thenReturn(listOf(informatieobjecttype1, informatieobjecttype2))

        val informatieobjecttypes = catalogiService.getInformatieobjecttypes(documentDefinitionName)

        assertEquals(informatieobjecttype1, informatieobjecttypes[0])
        assertEquals(informatieobjecttype2, informatieobjecttypes[1])
    }

    @Test
    fun `should return empty list if no plugin is found for zaak`() {
        val documentDefinitionName = "case-name"
        val zaaktypeUrl = URI("http://example.com/zaaktype")

        whenever(zaaktypeUrlProvider.getZaaktypeUrl(documentDefinitionName)).thenReturn(zaaktypeUrl)

        val result = catalogiService.getInformatieobjecttypes(documentDefinitionName)

        assertEquals(emptyList<Informatieobjecttype>(), result)
    }

}

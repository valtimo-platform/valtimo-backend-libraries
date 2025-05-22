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

package com.ritense.catalogiapi.service

import com.ritense.catalogiapi.BaseTest
import com.ritense.catalogiapi.CatalogiApiPlugin
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI

internal class CatalogiServiceTest : BaseTest() {

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

    @Test
    fun `should get zaaktypen using plugins`() {
        val pluginConfigurations = IntRange(0, 1).map {
            mock<PluginConfiguration>()
        }
        whenever(pluginService.findPluginConfigurations(CatalogiApiPlugin::class.java)).thenReturn(pluginConfigurations)

        val plugins = pluginConfigurations.mapIndexed { index, pluginConfiguration ->
            val plugin: CatalogiApiPlugin = mock()
            whenever(pluginService.createInstance(pluginConfiguration)).thenReturn(plugin)
            whenever(plugin.getZaaktypen()).thenReturn(listOf(newZaaktype(
                URI("example.com/$index"),
                "Zaak $index"
            )))
            plugin
        }

        val zaakTypen = catalogiService.getZaakTypen()

        assertThat(zaakTypen).hasSize(plugins.size)
        zaakTypen.forEachIndexed { index, zaaktype ->
            assertThat(zaaktype.url).isEqualTo(URI("example.com/$index"))
            assertThat(zaaktype.omschrijving).isEqualTo("Zaak $index")
        }
    }

}

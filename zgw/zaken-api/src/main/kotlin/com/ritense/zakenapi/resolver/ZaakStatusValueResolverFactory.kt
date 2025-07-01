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

package com.ritense.zakenapi.resolver

import com.ritense.catalogiapi.CatalogiApiPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import org.operaton.bpm.engine.delegate.VariableScope
import java.net.URI
import java.util.UUID
import java.util.function.Function

class ZaakStatusValueResolverFactory(
    processDocumentService: ProcessDocumentService,
    private val zaakUrlProvider: ZaakUrlProvider,
    private val pluginService: PluginService,
) : BaseFieldValueResolverFactory(processDocumentService) {
    override fun supportedPrefix(): String {
        return "zaakstatus"
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        val url = zaakUrlProvider.getZaakUrl(UUID.fromString(documentId))
        val zakenApiPlugin = getZakenApiPlugin(url)
        val zaakStatus = zakenApiPlugin.getZaakStatus(url) ?: return Function { null }
        val statusTypeUrl = zaakStatus.statustype
        val catalogiApiPlugin = getCatalogiApiPlugin(statusTypeUrl)
        val statusType = catalogiApiPlugin.getStatustype(statusTypeUrl)
        return Function { requestedValue ->
            return@Function getField(statusType, requestedValue)
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        TODO()
    }

    private fun getZakenApiPlugin(url: URI): ZakenApiPlugin {
        return pluginService.createInstance(
            ZakenApiPlugin::class.java,
            ZakenApiPlugin.findConfigurationByUrl(url)
        )
            ?: throw IllegalStateException("Missing plugin configuration of type '${ZakenApiPlugin.PLUGIN_KEY}' for url '$url'")
    }

    private fun getCatalogiApiPlugin(statusTypeUrl: URI): CatalogiApiPlugin {
        return pluginService.createInstance(
            CatalogiApiPlugin::class.java,
            CatalogiApiPlugin.findConfigurationByUrl(statusTypeUrl)
        )
            ?: throw IllegalStateException("Missing plugin configuration of type '${CatalogiApiPlugin.PLUGIN_KEY}' for statusType '$statusTypeUrl'")
    }
}

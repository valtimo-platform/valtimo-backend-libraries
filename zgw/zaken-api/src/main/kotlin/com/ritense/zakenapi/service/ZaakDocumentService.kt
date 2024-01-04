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

package com.ritense.zakenapi.service

import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.web.rest.dto.RelatedFileDto
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.UUID

@Transactional
class ZaakDocumentService(
    val zaakUrlProvider: ZaakUrlProvider,
    val pluginService: PluginService
) {

    fun getInformatieObjectenAsRelatedFiles(documentId: UUID): List<RelatedFileDto> {
        val zaakUri = zaakUrlProvider.getZaakUrl(documentId)

        val zakenApiPlugin = checkNotNull(
            pluginService.createInstance(
                ZakenApiPlugin::class.java,
                ZakenApiPlugin.findConfigurationByUrl(zaakUri)
            )
        ) { "Could not find ${ZakenApiPlugin::class.simpleName} configuration for zaak with url: $zaakUri" }

        return zakenApiPlugin.getZaakInformatieObjecten(zaakUri)
            .map { getRelatedFiles(it) }
    }

    private fun getRelatedFiles(zaakInformatieObject: ZaakInformatieObject): RelatedFileDto {
        val pluginConfiguration = getDocumentenApiPluginByInformatieobjectUrl(zaakInformatieObject.informatieobject)
        val plugin = pluginService.createInstance(pluginConfiguration) as DocumentenApiPlugin
        val informatieObject = plugin.getInformatieObject(zaakInformatieObject.informatieobject)
        return RelatedFileDto(
            fileId = UUID.fromString(informatieObject.url.path.substringAfterLast("/")),
            fileName = informatieObject.bestandsnaam,
            sizeInBytes = informatieObject.bestandsomvang,
            createdOn = informatieObject.creatiedatum.atStartOfDay(),
            createdBy = informatieObject.auteur,
            pluginConfigurationId = pluginConfiguration.id.id,
        )
    }

    private fun getDocumentenApiPluginByInformatieobjectUrl(informatieobjectUrl: URI): PluginConfiguration {
        return checkNotNull(
            pluginService.findPluginConfiguration(
                DocumentenApiPlugin::class.java,
                DocumentenApiPlugin.findConfigurationByUrl(informatieobjectUrl)
            )
        ) { "Could not find ${DocumentenApiPlugin::class.simpleName} configuration for informatieobjectUrl: $informatieobjectUrl" }

    }

    fun getZaakByDocumentId(documentId: UUID): ZaakResponse? {
        val url = try {
            zaakUrlProvider.getZaakUrl(documentId)
        } catch (e: ZaakInstanceLinkNotFoundException) {
            return null
        }
        val plugin = pluginService.createInstance(
            ZakenApiPlugin::class.java,
            ZakenApiPlugin.findConfigurationByUrl(url)
        )

        return plugin?.getZaak(url)
    }

    fun getZaakByDocumentIdOrThrow(documentId: UUID): ZaakResponse {
        val url = zaakUrlProvider.getZaakUrl(documentId)
        val plugin = pluginService.createInstance(
            ZakenApiPlugin::class.java,
            ZakenApiPlugin.findConfigurationByUrl(url)
        )
            ?: throw IllegalStateException("Missing plugin configuration of type '${ZakenApiPlugin.PLUGIN_KEY}' for url '$url'")
        return plugin.getZaak(url)
    }

}
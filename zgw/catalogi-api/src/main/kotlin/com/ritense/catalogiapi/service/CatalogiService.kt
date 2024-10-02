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

import com.ritense.catalogiapi.CatalogiApiPlugin
import com.ritense.catalogiapi.domain.Besluittype
import com.ritense.catalogiapi.domain.Eigenschap
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Resultaattype
import com.ritense.catalogiapi.domain.Roltype
import com.ritense.catalogiapi.domain.Statustype
import com.ritense.catalogiapi.exception.ZaakTypeLinkNotFoundException
import com.ritense.logging.LoggableResource
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.net.URI

@Service
@SkipComponentScan
class CatalogiService(
    val zaaktypeUrlProvider: ZaaktypeUrlProvider,
    val pluginService: PluginService
) {
    fun getInformatieobjecttypes(
        @LoggableResource("documentDefinitionName") documentDefinitionName: String
    ): List<Informatieobjecttype> {
        logger.debug { "Getting documenttypes for document definition $documentDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByDocumentDefinitionName(documentDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getInformatieobjecttypes(zaakTypeUrl)
    }

    fun getInformatieobjecttype(typeUrl: URI): Informatieobjecttype? {
        logger.debug { "Getting documenttype for with URL $typeUrl" }
        val catalogiApiPluginInstance = findCatalogiApiPlugin(typeUrl)

        return catalogiApiPluginInstance?.getInformatieobjecttype(typeUrl)
    }

    fun getRoltypes(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<Roltype> {
        logger.debug { "Getting roltypes for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getRoltypes(zaakTypeUrl)
    }

    fun getStatustypen(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<Statustype> {
        logger.debug { "Getting statustypen for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getStatustypen(zaakTypeUrl)
    }

    fun getResultaattypen(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<Resultaattype> {
        logger.debug { "Getting resultaattypen for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getResultaattypen(zaakTypeUrl)
    }

    fun getBesluittypen(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<Besluittype> {
        logger.debug { "Getting besluittypen for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getBesluittypen(zaakTypeUrl)
    }

    fun getEigenschappen(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<Eigenschap> {
        logger.debug { "Getting zgw eigenschappen for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getEigenschappen(zaakTypeUrl)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun prefillCache() {
        logger.debug { "Prefilling catalogi api cache" }
        try {
            pluginService.getPluginConfigurations(
                PluginConfigurationSearchParameters(
                    pluginDefinitionKey = "catalogiapi"
                )
            ).forEach {
                val catalogiApiPluginInstance = pluginService.createInstance(it) as CatalogiApiPlugin
                catalogiApiPluginInstance.prefillCache()
            }
        } catch (e: Exception) {
            // We don't want to crash the application if the cache prefilling fails
            logger.warn(e) { "Error while prefilling catalogi api cache" }
        }
    }

    private fun findCatalogiApiPlugin(catalogiContentUrl: URI): CatalogiApiPlugin? {
        val catalogiApiPluginInstance = pluginService
            .createInstance(CatalogiApiPlugin::class.java, CatalogiApiPlugin.findConfigurationByUrl(catalogiContentUrl))

        if (catalogiApiPluginInstance == null) {
            logger.error { "No catalogi plugin configuration was found for zaaktype with URL $catalogiContentUrl" }
        }

        return catalogiApiPluginInstance
    }

    private fun getZaaktypeUrlByDocumentDefinitionName(documentDefinitionName: String): URI? {
        return try {
            zaaktypeUrlProvider.getZaaktypeUrl(documentDefinitionName)
        } catch (e: ZaakTypeLinkNotFoundException) {
            logger.error { e }
            null
        }
    }

    private fun getZaaktypeUrlByCaseDefinitionName(caseDefinitionName: String): URI? {
        return try {
            zaaktypeUrlProvider.getZaaktypeUrlByCaseDefinitionName(caseDefinitionName)
        } catch (e: ZaakTypeLinkNotFoundException) {
            logger.error { e }
            null
        }
    }

    fun getZaakTypen() =
        pluginService.findPluginConfigurations(CatalogiApiPlugin::class.java)
            .map { config ->
                pluginService.createInstance(config) as CatalogiApiPlugin
            }
            .flatMap { plugin -> plugin.getZaaktypen() }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

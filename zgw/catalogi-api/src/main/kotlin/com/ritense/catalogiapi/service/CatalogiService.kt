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

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.catalogiapi.CatalogiApiPlugin
import com.ritense.catalogiapi.domain.Besluittype
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Resultaattype
import com.ritense.catalogiapi.domain.Roltype
import com.ritense.catalogiapi.domain.Statustype
import com.ritense.catalogiapi.exception.ZaakTypeLinkNotFoundException
import com.ritense.plugin.service.PluginService
import mu.KotlinLogging
import java.net.URI

class CatalogiService(
    val zaaktypeUrlProvider: ZaaktypeUrlProvider,
    val pluginService: PluginService
) {
    fun getInformatieobjecttypes(documentDefinitionName: String): List<Informatieobjecttype> {
        logger.debug { "Getting documenttypes for document definition $documentDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByDocumentDefinitionName(documentDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getInformatieobjecttypes(zaakTypeUrl)
    }

    fun getRoltypes(caseDefinitionName: String): List<Roltype> {
        logger.debug { "Getting roltypes for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getRoltypes(zaakTypeUrl)
    }

    fun getStatustypen(caseDefinitionName: String): List<Statustype> {
        logger.debug { "Getting statustypen for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getStatustypen(zaakTypeUrl)
    }

    fun getResultaattypen(caseDefinitionName: String): List<Resultaattype> {
        logger.debug { "Getting resultaattypen for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getResultaattypen(zaakTypeUrl)
    }

    fun getBesluittypen(caseDefinitionName: String): List<Besluittype> {
        logger.debug { "Getting besluittypen for case definition $caseDefinitionName" }
        val zaakTypeUrl = getZaaktypeUrlByCaseDefinitionName(caseDefinitionName) ?: return emptyList()
        val catalogiApiPluginInstance = findCatalogiApiPlugin(zaakTypeUrl) ?: return emptyList()

        return catalogiApiPluginInstance.getBesluittypen(zaakTypeUrl)
    }

    private fun findCatalogiApiPlugin(zaakTypeUrl: URI): CatalogiApiPlugin? {
        val catalogiApiPluginInstance = pluginService
            .createInstance(CatalogiApiPlugin::class.java, CatalogiApiPlugin.findConfigurationByUrl(zaakTypeUrl))

        if (catalogiApiPluginInstance == null) {
            logger.debug {"No catalogi plugin configuration was found for zaaktype with URL $zaakTypeUrl" }
        }

        return catalogiApiPluginInstance
    }

    private fun getZaaktypeUrlByDocumentDefinitionName(documentDefinitionName: String): URI? {
        return try {
            zaaktypeUrlProvider.getZaaktypeUrl(documentDefinitionName)
        } catch (e: ZaakTypeLinkNotFoundException) {
            logger.debug { e }
            null
        }
    }

    private fun getZaaktypeUrlByCaseDefinitionName(caseDefinitionName: String): URI? {
        return try {
            zaaktypeUrlProvider.getZaaktypeUrlByCaseDefinitionName(caseDefinitionName)
        } catch (e: ZaakTypeLinkNotFoundException) {
            logger.debug { e }
            null
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.objectenapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objecttypenapi.ObjectType
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZaakObject
import com.ritense.zakenapi.ZakenApiPlugin
import java.net.URI
import java.net.URL
import java.util.UUID

class ZaakObjectService(
    val zaakInstanceLinkService: ZaakInstanceLinkService,
    val pluginService : PluginService
) {
    fun getZaakObjectTypes(documentId: UUID): List<ObjectType> {
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl
        val pluginConfig = pluginService.findPluginConfiguration(ZakenApiPlugin.PLUGIN_KEY) { properties: JsonNode ->
            zaakUrl.toString().startsWith(properties.get("url").textValue())
        }
        requireNotNull(pluginConfig) { "No plugin configuration was found for zaak with URL ${zaakUrl}" }
        val zakenApiPluginInstance = pluginService.createInstance(pluginConfig) as ZakenApiPlugin

        return zakenApiPluginInstance.getZaakObjecten(zaakUrl)
            .mapNotNull {
                getObjectByZaakObjectUrl(it)
            }.map { it.type }
            .distinct()
            .mapNotNull {
                getObjectTypeByUrl(it)
            }
    }

    private fun getObjectByZaakObjectUrl(it: ZaakObject) : ObjectValue? {
        val objectUrl = it.`object`
        val objectPluginConfig =
            pluginService.findPluginConfiguration(ObjectenApiPlugin.PLUGIN_KEY) { properties: JsonNode ->
                objectUrl.toString().startsWith(properties.get("url").textValue())
            }?: return null
        val objectenApiPlugin = pluginService.createInstance(objectPluginConfig!!) as ObjectenApiPlugin
        return objectenApiPlugin.getObject(objectUrl)
    }

    private fun getObjectTypeByUrl(it: URL): ObjectType? {
        val objectTypePluginConfig =
            pluginService.findPluginConfiguration(ObjecttypenApiPlugin.PLUGIN_KEY) { properties: JsonNode ->
                it.toString().startsWith(properties.get("url").textValue())
            }?: return null

        val objectTypePluginInstance = pluginService.createInstance(objectTypePluginConfig) as ObjecttypenApiPlugin

        return objectTypePluginInstance.getObjectType(it)
    }

    fun getZaakObjecten(documentId: UUID, typeUrl: URI): List<Any> {
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl
        //TODO: get plugin configuration for zaak objecten
        //TODO: get zaak object urls by zaakURL (zaakapi plugin)
        //TODO: get plugin configurations for objecten by urls
        //TODO: get zaak objecten by type urls (object api plugin)

        TODO()
    }

}
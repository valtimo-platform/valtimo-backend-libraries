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

package com.ritense.objectenapi.listener

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.service.ZaakObjectConstants
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import org.springframework.context.event.EventListener
import java.net.URI
import java.time.LocalDate
import java.util.InvalidPropertiesFormatException
import java.util.UUID

class ZaakObjectListener(
    val pluginService: PluginService,
    val zaakObjectService: ZaakObjectService,
) {
    @EventListener(ExternalDataSubmittedEvent::class)
    fun handle(event: ExternalDataSubmittedEvent) {
        event.data[ZaakObjectConstants.ZAAKOBJECT_PREFIX]?.let { it ->
            it.entries.map {
                RequestedField(
                    it.key, it.value
                )
            }.groupBy {
                it.objectType
            }.forEach { objectTypeGroup ->
                val zaakObject = zaakObjectService.getZaakObjectOfTypeByName(event.documentId, objectTypeGroup.key)
                objectTypeGroup.value.forEach {
                    // For each requestedField update the value in the zaakObject record data
                    val startPath = it.path.substring(1)
                    findAndReplaceJsonPath(zaakObject.record.data!!, startPath, extractValue(it.value as JsonNode)!!.toString())
                }

                // The zaakObject.record.data has now been updated with the new values, update the object in the objecten api
                updateObject(zaakObject.url, event.documentId, objectTypeGroup.key, zaakObject.record)
            }
        }
    }

    private fun updateObject(objectUrl: URI, documentId: UUID, objectTypeName: String, objectRecord: ObjectRecord) {
        val objectType = zaakObjectService.getZaakObjectTypes(documentId).first() {
            it.name == objectTypeName
        }

        findObjectenApiPlugin(objectUrl)
            .objectUpdate(
                objectUrl,
                ObjectRequest(
                    objectType.url,
                    ObjectRecord(
                        typeVersion = objectRecord.typeVersion,
                        data = objectRecord.data,
                        startAt = LocalDate.now(),
                        correctionFor = objectRecord.index.toString()
                    )
                ))
    }

    /**
     * Tne json path of the value the application needs to update could be several layers deep like
     * /profile/adresses/street this method will (recursively) navigate to the value that needs to be updated
     */
    private fun findAndReplaceJsonPath(jsonNode: JsonNode, jsonPath: String, newValue: String) {
        // If the jsonPath no longer contains a '/' we've reached the node we should update
        if (!jsonPath.contains('/')) {
            (jsonNode as ObjectNode).put(jsonPath, newValue)
            return
        }

        val currentNodePath = jsonPath.substringBefore("/")
        if (jsonNode.has(currentNodePath)) {
            val currentNode = jsonNode.get(currentNodePath)
            val nextPath = jsonPath.substringAfter("/")
            findAndReplaceJsonPath(currentNode, nextPath, newValue)
        } else {
            throw InvalidPropertiesFormatException("Could not find the path $currentNodePath in the json $jsonNode")
        }
    }

    private fun findObjectenApiPlugin(objectUrl: URI): ObjectenApiPlugin {
        val objectenApiPluginInstance = pluginService
            .createInstance(ObjectenApiPlugin::class.java) { properties: JsonNode ->
                objectUrl.toString().startsWith(properties.get("url").textValue())
            }

        requireNotNull(objectenApiPluginInstance) { "No objecten plugin configuration was found for the URL $objectUrl" }

        return objectenApiPluginInstance
    }

    private fun extractValue(node: JsonNode): Any? {
        return when(node.nodeType) {
            JsonNodeType.ARRAY -> node
            JsonNodeType.BINARY -> node.binaryValue()
            JsonNodeType.BOOLEAN -> node.booleanValue()
            JsonNodeType.MISSING -> null
            JsonNodeType.NULL -> null
            JsonNodeType.NUMBER -> node.asLong()
            JsonNodeType.OBJECT -> node
            JsonNodeType.POJO -> node
            JsonNodeType.STRING -> node.textValue()
            else -> null
        }
    }

    class RequestedField(
        variableName: String,
        val value: Any
    ) {
        val objectType = variableName.substringBeforeLast(":")
        val path = variableName.substringAfterLast(":")
    }
}
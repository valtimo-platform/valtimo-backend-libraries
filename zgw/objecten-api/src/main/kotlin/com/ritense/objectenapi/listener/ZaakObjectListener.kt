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

package com.ritense.objectenapi.listener

import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.service.ZaakObjectConstants
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import org.springframework.context.event.EventListener

class ZaakObjectListener(
    private val pluginService: PluginService,
    private val zaakObjectService: ZaakObjectService,
) {
    @EventListener(ExternalDataSubmittedEvent::class)
    fun handle(event: ExternalDataSubmittedEvent) {
        event.data[ZaakObjectConstants.ZAAKOBJECT_PREFIX]?.let { zaakObjectMap ->
            zaakObjectMap.entries.map { entry ->
                RequestedField(
                    entry.key, entry.value
                )
            }.groupBy { requestedField ->
                requestedField.objectType
            }.forEach { objectTypeGroup ->
                val zaakObject = zaakObjectService.getZaakObjectOfTypeByName(event.documentId, objectTypeGroup.key)
                objectTypeGroup.value.forEach { requestedField ->
                    // For each requestedField update the value in the zaakObject record data
                    val startPath = requestedField.path.substring(1)
                    val newValueNode = getValueNode(requestedField.value)
                    findAndReplaceJsonPath(zaakObject.record.data!! as ObjectNode, startPath, newValueNode)
                }

                // The zaakObject.record.data has now been updated with the new values, update the object in the objecten api
                updateObject(zaakObject.url, event.documentId, objectTypeGroup.key, zaakObject.record)
            }
        }
    }

    private fun updateObject(objectUrl: URI, documentId: UUID, objectTypeName: String, objectRecord: ObjectRecord) {
        val objectType = zaakObjectService.getZaakObjectTypes(documentId).first {
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
     * The json path of the value the application needs to update could be several layers deep like
     * /profile/adresses/street this method will (recursively) navigate to the value that needs to be updated
     */
    private fun findAndReplaceJsonPath(parentNode: ObjectNode, jsonPath: String, newValue: ValueNode) {
        // If the jsonPath no longer contains a '/' we've reached the node we should update
        if (!jsonPath.contains('/')) {
            parentNode.set<ValueNode>(jsonPath, newValue)
            return
        }

        val propertyName = jsonPath.substringBefore("/")
        val currentNode = parentNode.with(propertyName)
        val nextPath = jsonPath.substringAfter("/")
        findAndReplaceJsonPath(currentNode, nextPath, newValue)
    }

    private fun findObjectenApiPlugin(objectUrl: URI): ObjectenApiPlugin {
        val objectenApiPluginInstance = pluginService
            .createInstance(ObjectenApiPlugin::class.java, ObjectenApiPlugin.findConfigurationByUrl(objectUrl))

        requireNotNull(objectenApiPluginInstance) { "No objecten plugin configuration was found for the URL $objectUrl" }

        return objectenApiPluginInstance
    }

    private fun getValueNode(node: Any?): ValueNode {
        if(node is ValueNode) {
            return node
        }

        //TODO: do some conversion?

        return NullNode.getInstance()
    }

    class RequestedField(
        variableName: String,
        val value: Any
    ) {
        val objectType = variableName.substringBeforeLast(":")
        val path = variableName.substringAfterLast(":")
    }
}
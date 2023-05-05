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

package com.ritense.form.domain.submission.formfield

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.Document
import com.ritense.resource.domain.TemporaryResourceSubmittedEvent
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileSubmittedEvent
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import java.util.UUID

/**
 * Upload field class to perform additional processing when submission has files.
 *
 * Payload example for a Upload field
 * "bijlagen": [
 *      {
 *          "storage": "url",
 *          "name": "test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf",
 *          "url": "https://console.test.valtimo.nl/api/v1/form-file?baseUrl=http%3A%2F%2Flocalhost%3A4200&project=&form=/test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf",
 *          "size": 391,
 *          "type": "text/rtf",
 *          "data": {
 *              "key": "test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf",
 *              "baseUrl": "http://localhost:4200",
 *              "project": "",
 *              "form": ""
 *          },
 *          "originalName": "test.rtf"
 *      }
 * ]
 */
data class UploadField(
    override val value: JsonNode,
    override val pointer: JsonPointer,
    override val applicationEventPublisher: ApplicationEventPublisher
) : FormField(value, pointer, applicationEventPublisher) {

    private var processed = false

    override fun preProcess(document: Document?) {
        logger.debug { "pre-processing UploadField[$pointer:${value.asText()}]" }
        if (processed) {
            logger.debug { "skipping pre-processing already processed" }
            return
        }
        processResources(document)
    }

    override fun postProcess(document: Document?) {
        logger.debug { "postProcessing UploadField[$pointer:${value.asText()}]" }
        if (processed) {
            logger.debug { "skipping post-processing already processed" }
            return
        }
        processResources(document)
    }

    private fun processResources(document: Document?) {
        if (document != null) {
            value.forEach { resourceNode ->
                val resourceId = getResourceId(resourceNode)
                if (resourceId != null) {
                    logger.debug { "file $resourceId" }
                    applicationEventPublisher.publishEvent(
                        DocumentRelatedFileSubmittedEvent(document.id()?.id, resourceId, document.definitionId().name())
                    )
                }

                val tempResourceId = getTempResourceId(resourceNode)
                if (tempResourceId != null) {
                    logger.debug { "tempfile $tempResourceId" }
                    applicationEventPublisher.publishEvent(
                        TemporaryResourceSubmittedEvent(
                            tempResourceId,
                            document.id()!!.id,
                            document.definitionId().name()
                        )
                    )
                }
            }
            processed = true
        }
    }

    private fun getResourceId(resourceNode: JsonNode): UUID? {
        val resourceId = getFieldAsTextOrNull(resourceNode, "/data/resourceId")
        return if (resourceId == null) {
            null
        } else {
            UUID.fromString(resourceId)
        }
    }

    private fun getTempResourceId(resourceNode: JsonNode): String? {
        return getFieldAsTextOrNull(resourceNode, "/id")
    }

    private fun getFieldAsTextOrNull(rootNode: JsonNode, path: String): String? {
        val node = rootNode.at(path)
        return if (node.isMissingNode || node.isNull) {
            null
        } else {
            node.asText()
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        fun isUploadComponent(jsonNode: ObjectNode): Boolean {
            return jsonNode.has("type")
                    && (jsonNode["type"].textValue().equals("file", ignoreCase = true) ||
                    jsonNode["type"].textValue().equals("valtimo-file", ignoreCase = true) ||
                    jsonNode["type"].textValue().equals("documenten-api-file", ignoreCase = true))
                    && jsonNode["input"].booleanValue()
                    && jsonNode.has(PROPERTY_KEY)
        }
    }

}

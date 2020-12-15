/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.formlink.domain.impl.submission.formfield

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.impl.JsonSchemaDocument
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher

abstract class FormField(
    open val value: JsonNode,
    open val pointer: JsonPointer,
    open val documentSupplier: () -> JsonSchemaDocument?,
    open val applicationEventPublisher: ApplicationEventPublisher
) {
    internal val logger = KotlinLogging.logger {}

    abstract fun preProcess()

    abstract fun postProcess()

    companion object Factory {
        const val PROPERTY_KEY = "key"

        fun getFormField(
            formData: JsonNode,
            objectNode: ObjectNode,
            documentSupplier: () -> JsonSchemaDocument?,
            applicationEventPublisher: ApplicationEventPublisher
        ): FormField? {
            when {
                isUploadComponent(objectNode) -> {
                    val jsonPointer = getJsonPointer(objectNode[PROPERTY_KEY].asText())
                    return UploadField(
                        getValue(formData, jsonPointer),
                        jsonPointer,
                        documentSupplier,
                        applicationEventPublisher
                    )
                }
                isButtonComponent(objectNode) -> {
                    return null //skip buttons
                }
                isDataFieldComponent(objectNode) -> {
                    val jsonPointer = getJsonPointer(objectNode[PROPERTY_KEY].asText())
                    return DataField(
                        getValue(formData, jsonPointer),
                        jsonPointer,
                        documentSupplier,
                        applicationEventPublisher
                    )
                }
                else -> return null
            }
        }

        private fun isButtonComponent(jsonNode: ObjectNode): Boolean {
            return (jsonNode.has("type")
                && jsonNode["type"].textValue().equals("button", ignoreCase = true)
                && jsonNode["input"].booleanValue()
                && jsonNode.has(PROPERTY_KEY))
        }

        private fun isDataFieldComponent(jsonNode: ObjectNode): Boolean {
            return (jsonNode.has("type")
                && jsonNode["input"].booleanValue()
                && jsonNode.has(PROPERTY_KEY))
        }

        private fun isUploadComponent(jsonNode: ObjectNode): Boolean {
            return (jsonNode.has("type")
                && jsonNode["type"].textValue().equals("file", ignoreCase = true)
                && jsonNode["input"].booleanValue()
                && jsonNode.has(PROPERTY_KEY))
        }

        private fun getJsonPointer(jsonPath: String): JsonPointer {
            return JsonPointer.valueOf("/" + jsonPath.replace(".", "/"))
        }

        fun getValue(formData: JsonNode, jsonPointer: JsonPointer): JsonNode {
            return formData.at(jsonPointer)
        }

    }

}
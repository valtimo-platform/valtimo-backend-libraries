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
import com.ritense.valtimo.contract.json.JsonPointerHelper
import org.springframework.context.ApplicationEventPublisher

abstract class FormField(
    open val value: JsonNode,
    open val pointer: JsonPointer,
    open val applicationEventPublisher: ApplicationEventPublisher
) {

    abstract fun preProcess(document: Document?)

    abstract fun postProcess(document: Document?)

    open fun appendValueToDocument(documentContent: ObjectNode) {
        JsonPointerHelper.appendJsonPointerTo(documentContent, pointer, value)
    }

    companion object {
        fun getFormField(
            formData: JsonNode,
            objectNode: ObjectNode,
            applicationEventPublisher: ApplicationEventPublisher
        ): FormField? {
            val jsonPointer = getJsonPointer(objectNode[PROPERTY_KEY].asText())
            val value = formData.at(jsonPointer)
            if (value.isNull || value.isMissingNode) {
                return null
            }
            return when {
                UploadField.isUploadComponent(objectNode) -> UploadField(value, jsonPointer, applicationEventPublisher)
                DataField.isDataFieldComponent(objectNode) -> DataField(value, jsonPointer, applicationEventPublisher)
                else -> null
            }
        }

        private fun getJsonPointer(jsonPath: String): JsonPointer {
            return JsonPointer.valueOf("/" + jsonPath.replace(".", "/"))
        }

        const val PROPERTY_KEY = "key"
    }

}

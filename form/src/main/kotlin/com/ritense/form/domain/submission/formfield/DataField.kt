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
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher

data class DataField(
    override val value: JsonNode,
    override val pointer: JsonPointer,
    override val applicationEventPublisher: ApplicationEventPublisher
) : FormField(value, pointer, applicationEventPublisher) {

    override fun preProcess(document: Document?) {
        logger.debug { "processing $this" }
    }

    override fun postProcess(document: Document?) {
        logger.debug { "postProcess $this" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        fun isDataFieldComponent(jsonNode: ObjectNode): Boolean {
            return (jsonNode.has("type")
                    && !jsonNode["type"].textValue().equals("button", ignoreCase = true)
                    && jsonNode["input"].booleanValue()
                    && jsonNode.has(PROPERTY_KEY))
        }
    }
}

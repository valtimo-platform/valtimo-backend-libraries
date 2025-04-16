/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ContainerNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.Document
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher

data class ComponentsField(
    override val value: JsonNode,
    override val pointer: JsonPointer,
    override val applicationEventPublisher: ApplicationEventPublisher,
    private val objectNode: ObjectNode,
) : FormField(value, pointer, applicationEventPublisher) {

    val childFormFields: List<FormField> = createChildFormFields()

    override fun preProcess(document: Document?) {
        logger.debug { "preProcess ComponentsField[$pointer:${value.asText()}]" }
        childFormFields.forEach { formField -> formField.preProcess(document) }
    }

    override fun postProcess(document: Document?) {
        logger.debug { "postProcess ComponentsField[$pointer:${value.asText()}]" }
        childFormFields.forEach { formField -> formField.postProcess(document) }
    }

    private fun createChildFormFields(): List<FormField> {
        val childComponents = objectNode["components"] as ArrayNode
        val childValues = if (value is ArrayNode) {
            value
        } else {
            listOf(value)
        }
        return childValues.flatMap { childValue ->
            childComponents.mapNotNull { childComponent ->
                if (childComponent is ObjectNode) {
                    getFormField(childValue, childComponent, applicationEventPublisher)
                } else {
                    null
                }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        fun isComponentsComponent(jsonNode: ObjectNode): Boolean {
            return jsonNode.has("components")
                && jsonNode["components"].isArray
                && jsonNode.has("input") && jsonNode["input"].booleanValue()
                && jsonNode.has(PROPERTY_KEY)
        }
    }
}

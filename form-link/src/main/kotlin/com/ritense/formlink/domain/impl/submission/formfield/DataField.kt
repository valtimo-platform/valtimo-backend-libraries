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

package com.ritense.formlink.domain.impl.submission.formfield

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.document.domain.impl.JsonSchemaDocument
import org.springframework.context.ApplicationEventPublisher

data class DataField(
    override val value: JsonNode,
    override val pointer: JsonPointer,
    override var documentSupplier: () -> JsonSchemaDocument?,
    override val applicationEventPublisher: ApplicationEventPublisher
) : FormField(value, pointer, documentSupplier, applicationEventPublisher) {

    override fun preProcess() {
        logger.debug { "processing $this" }
    }

    override fun postProcess() {
        logger.debug { "postProcess $this" }
    }
}
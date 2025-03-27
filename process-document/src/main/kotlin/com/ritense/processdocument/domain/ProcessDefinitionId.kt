/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
package com.ritense.processdocument.domain

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.ritense.valtimo.contract.utils.AssertionConcern
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
@JsonSerialize(using = ProcessDefinitionIdSerializer::class)
@JsonDeserialize(using = ProcessDefinitionIdDeserializer::class)
data class ProcessDefinitionId(
    @Column(name = "process_definition_id", columnDefinition = "VARCHAR(255)")
    val id: String
) {
    init {
        AssertionConcern.assertArgumentLength(id, 255, "id max length is 255")
    }
}

class ProcessDefinitionIdSerializer : JsonSerializer<ProcessDefinitionId>() {
    override fun serialize(value: ProcessDefinitionId, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.id)
    }
}

class ProcessDefinitionIdDeserializer : JsonDeserializer<ProcessDefinitionId>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ProcessDefinitionId {
        return ProcessDefinitionId(p.valueAsString)
    }
}
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

package com.ritense.formflow.domain.definition

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.formflow.domain.definition.configuration.FormFlowStepType
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "form_flow_step")
data class FormFlowStep(

    @EmbeddedId
    @JsonProperty("key")
    val id: FormFlowStepId,

    @Column(name = "title")
    val title: String? = null,

    @Type(value = JsonType::class)
    @Column(name = "next_steps", columnDefinition = "JSON")
    val nextSteps: List<FormFlowNextStep> = listOf(),

    @Type(value = JsonType::class)
    @Column(name = "on_back", columnDefinition = "JSON")
    val onBack: List<String> = listOf(),

    @Type(value = JsonType::class)
    @Column(name = "on_open", columnDefinition = "JSON")
    val onOpen: List<String> = listOf(),

    @Type(value = JsonType::class)
    @Column(name = "on_complete", columnDefinition = "JSON")
    val onComplete: List<String> = listOf(),

    @Type(value = JsonType::class)
    @Column(name = "type", columnDefinition = "JSON", nullable = false)
    val type: FormFlowStepType
) {
    constructor(
        id: FormFlowStepId,
        nextSteps: List<FormFlowNextStep> = listOf(),
        onBack: List<String> = listOf(),
        onOpen: List<String> = listOf(),
        onComplete: List<String> = listOf(),
        type: FormFlowStepType
    ) : this(id, null, nextSteps, onBack, onOpen, onComplete, type)
}
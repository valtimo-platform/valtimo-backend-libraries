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

package com.ritense.formflow.domain

import org.hibernate.annotations.Type
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.OneToMany
import javax.persistence.OrderBy

@Embeddable
class FormFlowInstanceContext(
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("order ASC")
    private val history: MutableList<FormFlowStepInstance> = mutableListOf(),
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "additional_properties", columnDefinition = "json", nullable = false)
    private val additionalProperties: MutableMap<String, Any>
) {
    fun getHistory() : List<FormFlowStepInstance> {
        return history
    }

    internal fun addStep(formFlowStepInstance: FormFlowStepInstance) {
        history.add(formFlowStepInstance)
    }

    fun getAdditionalProperties() : Map<String, Any> {
        return additionalProperties
    }
}
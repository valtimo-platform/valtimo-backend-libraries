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

package com.ritense.formflow.domain.definition

import com.fasterxml.jackson.annotation.JsonCreator
import com.ritense.formflow.domain.AbstractId
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne

@Embeddable
data class FormFlowStepId(

    @Column(name = "form_flow_step_key")
    val key: String,

    @ManyToOne(targetEntity = FormFlowDefinition::class, fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "form_flow_definition_key", referencedColumnName = "form_flow_definition_key"),
        JoinColumn(name = "form_flow_definition_version", referencedColumnName = "form_flow_definition_version")
    )
    var formFlowDefinition: FormFlowDefinition? = null
) : AbstractId<FormFlowStepId>() {

    override fun hashCode(): Int {
        return Objects.hash(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormFlowStepId

        if (key != other.key) return false

        return true
    }

    override fun toString(): String {
        return "${formFlowDefinition?.id}:$key"
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(value: String) = FormFlowStepId(value).newIdentity()
    }
}
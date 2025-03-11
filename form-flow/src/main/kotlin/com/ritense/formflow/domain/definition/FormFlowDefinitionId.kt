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

import com.ritense.formflow.domain.AbstractId
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import java.util.Objects

@Embeddable
data class FormFlowDefinitionId(

    @Column(name = "form_flow_definition_key")
    val key: String,

    @Embedded
    val caseDefinitionId: CaseDefinitionId

) : AbstractId<FormFlowDefinitionId>() {

    override fun toString(): String {
        return key
    }

    override fun hashCode(): Int {
        return Objects.hash(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormFlowDefinitionId

        if (key != other.key) return false

        return true
    }

    companion object {
        fun newId(key: String, caseDefinitionId: CaseDefinitionId): FormFlowDefinitionId {
            return FormFlowDefinitionId(key, caseDefinitionId).newIdentity()
        }

        fun existingId(id: FormFlowDefinitionId): FormFlowDefinitionId {
            return FormFlowDefinitionId(id.key, id.caseDefinitionId)
        }

        fun existingId(key: String, caseDefinitionId: CaseDefinitionId): FormFlowDefinitionId {
            return FormFlowDefinitionId(key, caseDefinitionId)
        }
    }
}
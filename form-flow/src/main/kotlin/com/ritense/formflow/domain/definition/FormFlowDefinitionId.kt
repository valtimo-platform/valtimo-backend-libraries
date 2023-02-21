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

import com.ritense.formflow.domain.AbstractId
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class FormFlowDefinitionId(

    @Column(name = "form_flow_definition_key")
    val key: String,

    @Column(name = "form_flow_definition_version")
    val version: Long

) : AbstractId<FormFlowDefinitionId>() {

    override fun toString(): String {
        return "$key:$version"
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
        fun newId(key: String): FormFlowDefinitionId {
            return FormFlowDefinitionId(key, 1).newIdentity()
        }

        fun nextVersion(id: FormFlowDefinitionId): FormFlowDefinitionId {
            return FormFlowDefinitionId(id.key, id.version!! + 1).newIdentity()
        }

        fun existingId(id: FormFlowDefinitionId): FormFlowDefinitionId {
            return FormFlowDefinitionId(id.key, id.version)
        }
    }
}
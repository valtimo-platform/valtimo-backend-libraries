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

package com.ritense.formflow.domain.instance

import com.ritense.formflow.domain.AbstractId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class FormFlowInstanceId(
    @Column(name = "form_flow_instance_id")
    val id: UUID
) : AbstractId<FormFlowInstanceId>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormFlowInstanceId

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun newId() : FormFlowInstanceId {
            return FormFlowInstanceId(UUID.randomUUID()).newIdentity()
        }

        fun existingId(id: UUID): FormFlowInstanceId {
            return FormFlowInstanceId(id)
        }

        fun existingId(id: String): FormFlowInstanceId {
            return existingId(UUID.fromString(id))
        }
    }
}

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

package com.ritense.form.domain

import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
class IntermediateSubmissionId(
    @Column(name = "id", updatable = false)
    val id: UUID
) : AbstractId<IntermediateSubmissionId>() {

    companion object {

        fun existingId(id: UUID): IntermediateSubmissionId {
            return IntermediateSubmissionId(id)
        }

        fun newId(id: UUID): IntermediateSubmissionId {
            return IntermediateSubmissionId(id).newIdentity()
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntermediateSubmissionId

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
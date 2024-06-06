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
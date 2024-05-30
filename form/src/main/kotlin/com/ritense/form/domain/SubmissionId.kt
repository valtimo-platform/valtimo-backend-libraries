package com.ritense.form.domain

import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
class SubmissionId(
    @Column(name = "submission_id", updatable = false)
    val id: UUID
) : AbstractId<SubmissionId>() {

    companion object {

        fun existingId(id: UUID): SubmissionId {
            return SubmissionId(id)
        }

        fun newId(id: UUID): SubmissionId {
            return SubmissionId(id).newIdentity()
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubmissionId

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
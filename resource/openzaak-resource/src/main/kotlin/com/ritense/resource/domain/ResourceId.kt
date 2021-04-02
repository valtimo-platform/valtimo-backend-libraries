package com.ritense.resource.domain

import com.fasterxml.jackson.annotation.JsonValue
import com.ritense.valtimo.contract.domain.AbstractId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class ResourceId(

    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    @JsonValue
    val id: UUID

) : AbstractId<ResourceId>() {

    companion object {
        fun existingId(id: UUID): ResourceId {
            return ResourceId(id)
        }

        fun newId(id: UUID): ResourceId {
            return ResourceId(id).newIdentity()
        }
    }
}
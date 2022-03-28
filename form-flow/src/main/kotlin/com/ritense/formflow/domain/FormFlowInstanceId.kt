package com.ritense.formflow.domain

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class FormFlowInstanceId(
    @Column(name = "form_flow_instance_id")
    val id: UUID
) : AbstractId<FormFlowInstanceId>() {
    companion object {
        fun newId() : FormFlowInstanceId {
            return FormFlowInstanceId(UUID.randomUUID()).newIdentity()
        }

        fun existingId(id: UUID): FormFlowInstanceId {
            return FormFlowInstanceId(id)
        }
    }
}
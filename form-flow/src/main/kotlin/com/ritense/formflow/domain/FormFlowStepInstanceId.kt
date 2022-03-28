package com.ritense.formflow.domain

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class FormFlowStepInstanceId(
    @Column(name = "form_flow_step_instance_id")
    val id: UUID
) : AbstractId<FormFlowStepInstanceId>() {
    companion object {
        fun newId() : FormFlowStepInstanceId {
            return FormFlowStepInstanceId(UUID.randomUUID()).newIdentity()
        }

        fun existingId(id: UUID): FormFlowStepInstanceId {
            return FormFlowStepInstanceId(id)
        }
    }
}
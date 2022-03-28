package com.ritense.formflow.domain

import javax.persistence.AttributeOverride
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "form_flow_instance")
data class FormFlowInstance(
    @EmbeddedId
    val id: FormFlowInstanceId,
    @Embedded
    val formFlowDefinitionId: FormFlowDefinitionId,
    @Embedded
    val context: FormFlowInstanceContext,
    @Embedded
    @AttributeOverride(name = "id", column = Column(name = "current_form_flow_step_instance_id"))
    val currentFormFlowStepInstanceId: FormFlowStepInstanceId
) {
    fun complete(
        currentFormFlowStepInstanceId: FormFlowStepInstanceId,
        submissionData: String
    ) : FormFlowStepInstanceId {
        assert(this.currentFormFlowStepInstanceId == currentFormFlowStepInstanceId)

        val formFlowStepInstance = context.history
            .filter { formFlowStepInstance -> formFlowStepInstance.id == currentFormFlowStepInstanceId }
            .first()

        formFlowStepInstance.submissionData = submissionData
        return FormFlowStepInstance(instance = this, stepKey = "henk", order = context.history.size).id
    }
}
package com.ritense.formflow.domain

import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "form_flow_step_instance")
data class FormFlowStepInstance(
    @EmbeddedId
    val id: FormFlowStepInstanceId = FormFlowStepInstanceId.newId(),
    @Column(name = "form_flow_instance_id", updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val instance: FormFlowInstance,
    @Column(name = "form_flow_step_key", updatable = false, nullable = false)
    val stepKey: String,
    @Column(name = "order", updatable = false, nullable = false)
    val order: Int,
    @Column(name = "submission_data")
    var submissionData: String? = null
)
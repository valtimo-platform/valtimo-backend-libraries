package com.ritense.valtimo.formflow.event


data class FormFlowStepCompleted(
    val id: String? = null,
    val instanceId: String? = null,
    val stepKey: String? = null,
    val order: Int? = null,
    val submissionData: String? = null,
    val additionalProperties: Map<String, Any>? = null,
)
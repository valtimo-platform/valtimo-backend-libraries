package com.ritense.formflow.repository

import com.ritense.formflow.domain.instance.FormFlowInstance

interface FormFlowAdditionalPropertiesSearchRepository {
    fun findInstances(additionalProperties: Map<String, Any>): List<FormFlowInstance>
}
package com.ritense.formflow.repository

import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import org.springframework.data.jpa.repository.JpaRepository

interface FormFlowStepInstanceRepository: JpaRepository<FormFlowStepInstance, FormFlowStepInstanceId>
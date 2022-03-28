package com.ritense.formflow.repository

import com.ritense.formflow.domain.FormFlowStepInstance
import com.ritense.formflow.domain.FormFlowStepInstanceId
import org.springframework.data.jpa.repository.JpaRepository

interface FormFlowStepInstanceRepository: JpaRepository<FormFlowStepInstance, FormFlowStepInstanceId>
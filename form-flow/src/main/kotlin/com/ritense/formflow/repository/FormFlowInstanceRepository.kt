package com.ritense.formflow.repository

import com.ritense.formflow.domain.FormFlowInstance
import com.ritense.formflow.domain.FormFlowInstanceId
import org.springframework.data.jpa.repository.JpaRepository

interface FormFlowInstanceRepository: JpaRepository<FormFlowInstance, FormFlowInstanceId>
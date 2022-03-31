package com.ritense.formflow.repository

import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import org.springframework.data.jpa.repository.JpaRepository

interface FormFlowInstanceRepository: JpaRepository<FormFlowInstance, FormFlowInstanceId>
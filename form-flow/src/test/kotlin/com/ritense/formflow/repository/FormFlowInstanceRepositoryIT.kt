package com.ritense.formflow.repository

import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.domain.FormFlowInstance
import org.junit.jupiter.api.Test
import javax.inject.Inject

internal class FormFlowInstanceRepositoryIT : BaseIntegrationTest() {
    @Inject
    private lateinit var formFlowInstanceRepository: FormFlowInstanceRepository

//    @Test
//    fun `create form flow instance successfully`() {
//        val formFlowInstance = FormFlowInstance()
//        formFlowInstanceRepository.save(formFlowInstance)
//    }
//
//    @Test
//    fun `update form flow instance successfully`() {
//
//    }
//
//    @Test
//    fun `delete form flow instance successfully`() {
//
//    }
}
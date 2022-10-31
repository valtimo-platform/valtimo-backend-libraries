package com.ritense.formflow.service

import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

internal class FormFlowServiceIntTest: BaseIntegrationTest() {
    @Autowired
    lateinit var formFlowService: FormFlowService

    @Autowired
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository

    @BeforeEach
    fun setUp() {
        formFlowInstanceRepository.deleteAll()
    }

    @Test
    fun `finds 2 formFlowInstances for 1 additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        val formFlowInstance = definition.createInstance(mutableMapOf("taskId" to "123"))
        formFlowInstance.getHistory()
        formFlowInstance.complete(
            formFlowInstance.currentFormFlowStepInstanceId!!,
            JSONObject("{\"step1\":\"A\"}"))
        formFlowService.save(formFlowInstance)
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        assertEquals(2, formFlowService.findInstances(mutableMapOf("taskId" to "123")).size)
    }

    @Transactional
    @Test
    fun `finds 1 formFlowInstance for 1 additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        val formFlowInstance = definition.createInstance(mutableMapOf("taskId" to "123"))
        formFlowInstance.getHistory()
        formFlowInstance.complete(
            formFlowInstance.currentFormFlowStepInstanceId!!,
            JSONObject("{\"step1\":\"A\"}"))
        formFlowService.save(formFlowInstance)
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "12345")))
        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId" to "123")).size)
    }

//    TODO: Make this working for MySQL
//    @Test
//    fun `finds 1 formFlowInstance for 1 complex additionalProperty`() {
//        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
//        val henk = formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "123"))))
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "1234"))))
//        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "123"))).size)
//    }

//    TODO: Implement this as a feature
//    @Test
//    fun `finds 1 formFlowInstance for 1 complex additionalProperty with complex path`() {
//        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "123"))))
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "1234"))))
//        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId.actualTaskId" to "123")).size)
//    }

//    @Test
//    fun `finds 0 formFlowInstances for 1 additionalProperty`() {
//        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "1234")))
//        assertEquals(0, formFlowService.findInstances(mutableMapOf("documentId" to "123")).size)
//    }
//
//    @Test
//    fun `finds 2 formFlowInstances for 2 additionalProperties`() {
//        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
//        formFlowService.save(
//            definition.createInstance(
//                mutableMapOf(
//                    "taskId" to "123",
//                    "documentId" to "456",
//                    "somethingId" to 123
//                )
//            )
//        )
//        formFlowService.save(
//            definition.createInstance(
//                mutableMapOf(
//                    "taskId" to "123",
//                    "documentId" to "456",
//                    "somethingId" to 124
//                )
//            )
//        )
//        formFlowService.save(
//            definition.createInstance(
//                mutableMapOf(
//                    "taskId" to "123",
//                    "documentId" to "457",
//                    "somethingId" to 123
//                )
//            )
//        )
//        assertEquals(
//            2,
//            formFlowService.findInstances(
//                mutableMapOf(
//                    "taskId" to "123",
//                    "documentId" to "456"
//                )
//            ).size
//        )
//    }
//
//    @Test
//    fun `finds 1 formFlowInstance for 1 non-string additionalProperty`() {
//        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to 123)))
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to 1234)))
//        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId" to 123)).size)
//    }
}
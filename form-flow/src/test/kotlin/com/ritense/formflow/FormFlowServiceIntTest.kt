package com.ritense.formflow

import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formflow.service.FormFlowService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

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
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        assertEquals(2, formFlowService.findInstances(mutableMapOf("taskId" to "123")).size)
    }

    @Test
    fun `finds 1 formFlowInstance for 1 additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "1234")))
        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId" to "123")).size)
    }



    @Test
    fun `finds 0 formFlowInstances for 1 additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "1234")))
        assertEquals(0, formFlowService.findInstances(mutableMapOf("documentId" to "123")).size)
    }

    @Test
    fun `finds 2 formFlowInstances for 2 additionalProperties`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(
            definition.createInstance(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "456",
                    "somethingId" to 123
                )
            )
        )
        formFlowService.save(
            definition.createInstance(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "456",
                    "somethingId" to 124
                )
            )
        )
        formFlowService.save(
            definition.createInstance(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "457",
                    "somethingId" to 123
                )
            )
        )
        assertEquals(
            2,
            formFlowService.findInstances(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "456"
                )
            ).size
        )
    }
}
package com.ritense.valtimo.formflow.handler

import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.formflow.BaseIntegrationTest
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FormFlowStepTypeCustomComponentHandlerIT: BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Test
    fun `should find correct properties for custom angular type step`() {
        val formFlowInstance = formFlowService
            .findDefinition("custom-component-test:latest")!!
            .createInstance(emptyMap())

        val stepProperties = formFlowService.getTypeProperties(formFlowInstance.getCurrentStep())

        assertEquals(CustomComponentTypeProperties::class, stepProperties::class)

        val customComponentTypeProperties = stepProperties as CustomComponentTypeProperties

        assertEquals("my-component-definition", customComponentTypeProperties.id)
    }
}
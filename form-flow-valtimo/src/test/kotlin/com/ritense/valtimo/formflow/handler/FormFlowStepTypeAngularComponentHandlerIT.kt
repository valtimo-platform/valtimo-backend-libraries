package com.ritense.valtimo.formflow.handler

import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.formflow.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class FormFlowStepTypeAngularComponentHandlerIT: BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Test
    fun `should find correct properties for custom angular type step`() {
        val formFlowInstance = formFlowService
            .findDefinition("custom-angular-test:latest")!!
            .createInstance(emptyMap())

        val stepProperties = formFlowService.getTypeProperties(formFlowInstance.getCurrentStep())

        assertEquals(AngularComponentTypeProperties::class, stepProperties::class)

        val angularComponentTypeProperties = stepProperties as AngularComponentTypeProperties

        assertEquals("my-component-definition", angularComponentTypeProperties.id)
    }
}
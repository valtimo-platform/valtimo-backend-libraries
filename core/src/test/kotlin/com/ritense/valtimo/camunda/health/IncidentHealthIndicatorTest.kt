package com.ritense.valtimo.camunda.health

import com.ritense.valtimo.BaseIntegrationTest
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.impl.IncidentQueryImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health

class IncidentHealthIndicatorTest {
    lateinit var runtimeService: RuntimeService
    lateinit var indicator: IncidentHealthIndicator

    @BeforeEach
    fun beforeEach() {
        runtimeService = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
        indicator = IncidentHealthIndicator(runtimeService)
    }

    @Test
    fun `should return up when no incidents exist`(){
        whenever(runtimeService.createIncidentQuery().count()).thenReturn(0L)
        val health = indicator.health()
        assertEquals(Health.up().build(), health)
    }

    @Test
    fun `should return unknown when incidents exist`(){
        whenever(runtimeService.createIncidentQuery().count()).thenReturn(2L)
        val health = indicator.health()
        assertEquals(Health.unknown().build(), health)
    }
}
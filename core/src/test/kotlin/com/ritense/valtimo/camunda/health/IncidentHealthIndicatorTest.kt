package com.ritense.valtimo.camunda.health

import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
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
        assertEquals(Health.up().build(), indicator.health())
    }

    @Test
    fun `should return unknown when incidents exist`(){
        whenever(runtimeService.createIncidentQuery().count()).thenReturn(2L)
        assertEquals(Health.unknown().build(), indicator.health())
    }
}
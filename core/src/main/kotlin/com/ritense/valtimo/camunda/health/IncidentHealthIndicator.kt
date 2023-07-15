package com.ritense.valtimo.camunda.health

import org.camunda.bpm.engine.RuntimeService
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status

class IncidentHealthIndicator (
    private val runtimeService: RuntimeService
) : AbstractHealthIndicator() {
    override fun doHealthCheck(builder: Health.Builder?) {
        val incidentCount = runtimeService.createIncidentQuery().count()
        builder!!.status(if (incidentCount == 0L) Status.UP else Status.UNKNOWN)
    }
}
package com.ritense.valtimo.camunda.health;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

public class IncidentHealthIndicator extends AbstractHealthIndicator {
    private final RuntimeService runtimeService;

    public IncidentHealthIndicator(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        long incidentCount = runtimeService.createIncidentQuery().count();
        builder.status(incidentCount == 0 ? Status.UP : Status.UNKNOWN);
    }
}

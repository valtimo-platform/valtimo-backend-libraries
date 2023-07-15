package com.ritense.valtimo.camunda.health;

import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IncidentHealthIndicatorTest {
    private IncidentHealthIndicator incidentHealthIndicator;
    private Health.Builder incidentHealthBuilder;
    private RuntimeService runtimeService;

    @BeforeEach
    public void setUp() {
        incidentHealthBuilder = new Health.Builder();
        runtimeService = mock(RuntimeService.class, RETURNS_DEEP_STUBS);
        incidentHealthIndicator = new IncidentHealthIndicator(runtimeService);
    }

    @Test
    public void doHealthCheck_NoIncidents_HealthUp() throws Exception {
        when(runtimeService.createIncidentQuery().count()).thenReturn(0L);

        incidentHealthIndicator.doHealthCheck(incidentHealthBuilder);

        assertEquals(healthUp(), incidentHealthBuilder.build());
    }

    @Test
    public void doHealthCheck_Incidents_HealthUnknown() throws Exception {
        when(runtimeService.createIncidentQuery().count()).thenReturn(1L);

        incidentHealthIndicator.doHealthCheck(incidentHealthBuilder);

        assertEquals(healthUnknown(), incidentHealthBuilder.build());
    }

    private Health healthUp() {
        return new Health.Builder().up().build();
    }
    private Health healthUnknown() {
        return new Health.Builder().unknown().build();
    }
}
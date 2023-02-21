/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.mail;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.ritense.mail.config.MandrillProperties;
import com.ritense.mail.service.MandrillHealthIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import static com.ritense.mail.service.MandrillHealthIndicator.PONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MandrillHealthIndicatorTest {

    private MandrillHealthIndicator mandrillHealthIndicator;
    private MandrillProperties mandrillProperties;
    private MandrillApi mandrillApi;
    private Health.Builder mandrillHealthBuilder;

    @BeforeEach
    public void setUp() {
        mandrillHealthBuilder = new Health.Builder();
    }

    @Test
    public void doHealthCheckWithoutMandrillProperties() {
        assertThrows(NullPointerException.class, () -> {
            mandrillHealthIndicator = new MandrillHealthIndicator(null);
        });
    }

    @Test
    public void doHealthCheckWithNullKey() {
        mandrillProperties = new MandrillProperties();
        mandrillProperties.setApiKey(null);
        assertThrows(NullPointerException.class, () -> {
            mandrillHealthIndicator = new MandrillHealthIndicator(mandrillProperties);
        });
    }

    @Test
    public void doHealthCheck_NotPong_HealthUnknown() throws Exception {
        mandrillApi = mock(MandrillApi.class, RETURNS_DEEP_STUBS);
        when(mandrillApi.users().ping()).thenReturn("NOT_PONG");

        mandrillProperties = mock(MandrillProperties.class);
        when(mandrillProperties.createMandrillApi()).thenReturn(mandrillApi);

        mandrillHealthIndicator = new MandrillHealthIndicator(mandrillProperties);

        mandrillHealthIndicator.doHealthCheck(mandrillHealthBuilder);
        assertEquals(healthUnknown(), mandrillHealthBuilder.build());
    }

    @Test
    public void doHealthCheck_ThrowsException_HealthDown() throws Exception {
        mandrillApi = mock(MandrillApi.class, RETURNS_DEEP_STUBS);
        when(mandrillApi.users().ping()).thenThrow(new MandrillApiError());

        mandrillProperties = mock(MandrillProperties.class);
        when(mandrillProperties.createMandrillApi()).thenReturn(mandrillApi);

        mandrillHealthIndicator = new MandrillHealthIndicator(mandrillProperties);

        mandrillHealthIndicator.doHealthCheck(mandrillHealthBuilder);
        assertEquals(healthDown(), mandrillHealthBuilder.build());
    }

    @Test
    public void doHealthCheck_IsPong_HealthUp() throws Exception {
        mandrillApi = mock(MandrillApi.class, RETURNS_DEEP_STUBS);
        when(mandrillApi.users().ping()).thenReturn(PONG);

        mandrillProperties = mock(MandrillProperties.class);
        when(mandrillProperties.createMandrillApi()).thenReturn(mandrillApi);

        mandrillHealthIndicator = new MandrillHealthIndicator(mandrillProperties);

        mandrillHealthIndicator.doHealthCheck(mandrillHealthBuilder);
        assertEquals(healthUp(), mandrillHealthBuilder.build());
    }

    private Health healthUp() {
        return new Health.Builder().up().build();
    }

    private Health healthUnknown() {
        return new Health.Builder().unknown().build();
    }

    private Health healthDown() {
        return new Health.Builder().down().build();
    }
}
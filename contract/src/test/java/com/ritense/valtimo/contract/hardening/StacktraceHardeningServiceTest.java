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

package com.ritense.valtimo.contract.hardening;

import com.ritense.valtimo.contract.hardening.config.HardeningProperties;
import com.ritense.valtimo.contract.hardening.service.HardeningService;
import com.ritense.valtimo.contract.hardening.service.impl.HardeningServiceImpl;
import com.ritense.valtimo.contract.hardening.throwable.UnsanitizedThrowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StacktraceHardeningServiceTest {
    private HardeningService hardeningService;
    private HttpServletRequest req;
    private HardeningProperties hardeningProperties;

    @BeforeEach
    public void setUp() {
        req = mock(HttpServletRequest.class);
        hardeningProperties = mock(HardeningProperties.class);
        hardeningService = new HardeningServiceImpl(hardeningProperties);
    }

    private Throwable exceptionWithoutProperCause() {
        Exception ex = null;

        try {
            Charset.forName("random charset");
        } catch (Exception caughtEx) {
            ex = caughtEx;
        }

        return ex;
    }

    private Throwable exceptionWithCause() {
        return new Throwable("Psst, there's a nested exception", exceptionWithoutProperCause());
    }

    private Throwable runTestFor(Supplier<Throwable> throwableSupplier, List<String> whitelistedIps, String remoteAddress, String forwardedForIps) {
        when(hardeningProperties.getAllowStacktraceOnIps()).then((invocationMock) -> new HashSet(whitelistedIps));
        when(req.getRemoteAddr()).then((invocationMock) -> "127.0.0.1");

        if (forwardedForIps == null || forwardedForIps.isEmpty()) {
            // GetHeader returns null if the key is not present. Empty XForwardedFor does not make sense in a Request object
            // but it might occur in the tests. So handle that here and just set the mock to return null in these cases.
            when(req.getHeader("X-Forwarded-For")).then((invocationMock) -> null);
        } else {
            when(req.getHeader("X-Forwarded-For")).then((invocationMock) -> forwardedForIps);
        }

        Throwable throwable = throwableSupplier.get();
        return hardeningService.harden(throwable, req);
    }

    @Test
    public void hardeningKeepsStacktraceIntactWhenWhitelisted_remoteAddr() {
        List<String> whitelistedIps = Arrays.asList("192.168.1.1", "192.168.1.2", "127.0.0.1");
        String remoteAddress = "127.0.0.1";

        UnsanitizedThrowable hardenedThrowable = (UnsanitizedThrowable) runTestFor(this::exceptionWithoutProperCause, whitelistedIps, remoteAddress, "");
        assertNotEquals(0, hardenedThrowable.getStackTrace().length);
        assertEquals("IP address is whitelisted: 127.0.0.1", hardenedThrowable.getReasonNotSanitized());
    }

    @Test
    public void hardeningKeepsStacktraceIntactWhenWhitelisted_forwardedFor() {
        List<String> whitelistedIps = Arrays.asList("192.168.1.1", "192.168.1.2", "127.0.0.1");
        String remoteAddress = "123.456.789.777";
        String forwardedForIps = "123.456.789,645.12.45.98,127.0.0.1";

        UnsanitizedThrowable hardenedThrowable = (UnsanitizedThrowable) runTestFor(this::exceptionWithoutProperCause, whitelistedIps, remoteAddress, forwardedForIps);
        assertNotEquals(0, hardenedThrowable.getStackTrace().length);
        assertEquals("IP address is whitelisted: 127.0.0.1", hardenedThrowable.getReasonNotSanitized());
    }

    @Test
    public void hardeningWipesTheStacktraceWhenNotWhitelisted() {
        List<String> whitelistedIps = Arrays.asList("192.168.1.1", "192.168.1.2");
        String remoteAddress = "123.456.789.777";
        String forwardedForIps = "123.456.789,645.12.45.98,127.0.0.1";

        Throwable hardenedThrowable = runTestFor(this::exceptionWithoutProperCause, whitelistedIps, remoteAddress, forwardedForIps);
        assertEquals(0, hardenedThrowable.getStackTrace().length);
    }

    @Test
    public void hardeningDoesntWipeTheStacktraceWhenWhitelistHasWildcard() {
        List<String> whitelistedIps = Arrays.asList("*");
        String remoteAddress = "123.456.789.777";
        String forwardedForIps = "123.456.789,645.12.45.98,127.0.0.1";

        UnsanitizedThrowable hardenedThrowable = (UnsanitizedThrowable) runTestFor(this::exceptionWithoutProperCause, whitelistedIps, remoteAddress, forwardedForIps);
        assertNotEquals(0, hardenedThrowable.getStackTrace().length);
        assertEquals("IP address is whitelisted: *", hardenedThrowable.getReasonNotSanitized());
    }

    @Test
    public void hardeningAlsoSanitizesNestedThrowable() {
        List<String> whitelistedIps = Arrays.asList("");
        String remoteAddress = "123.456.789.777";
        String forwardedForIps = "123.456.789,645.12.45.98,127.0.0.1";

        int numExceptionsInChainAsserted = 0;
        Throwable hardenedThrowable = runTestFor(this::exceptionWithCause, whitelistedIps, remoteAddress, forwardedForIps);

        // iteratively traverse the exception->cause chain and assert that each exception (and thefore also the cause in the next loop iteration)
        // has an empty stacktrace
        while (true) {
            assertEquals(0, hardenedThrowable.getStackTrace().length);
            numExceptionsInChainAsserted++;

            // keep getting causes until exhausted the chain
            if (hardenedThrowable.getCause() != hardenedThrowable && hardenedThrowable.getCause() != null) {
                hardenedThrowable = hardenedThrowable.getCause();
            } else {
                break;
            }
        }

        // ensure this test case has tested an exception with at least one nested exception
        assertTrue(numExceptionsInChainAsserted >= 2, "Throwable must have at least one nested exception");
        // asserted all the exceptions and their causes
        return;
    }

}
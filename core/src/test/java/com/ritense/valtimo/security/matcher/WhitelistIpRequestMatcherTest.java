/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.security.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WhitelistIpRequestMatcherTest {

    private static final String IPV4_LOOPBACK_IP = "127.0.0.1";
    private static final String LOCALHOST = "localhost";
    private WhitelistIpRequestMatcher localHostRequestMatcher;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        localHostRequestMatcher = new WhitelistIpRequestMatcher(Set.of(LOCALHOST));
        httpServletRequest = mock(HttpServletRequest.class);
    }

    @Test
    void shouldReturnTrueWhenIpFromLocalHost() {
        when(httpServletRequest.getRemoteAddr()).thenReturn(IPV4_LOOPBACK_IP);

        final boolean result = localHostRequestMatcher.matches(httpServletRequest);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmpty() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("");

        final boolean result = localHostRequestMatcher.matches(httpServletRequest);
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNonLocalHostIp() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("10.0.0.1");

        final boolean result = localHostRequestMatcher.matches(httpServletRequest);
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenWhitelistedWithNetmask() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("10.0.0.1");

        var privateNetworkMatcher = new WhitelistIpRequestMatcher(Set.of("10.0.0.0/8"));
        final boolean result = privateNetworkMatcher.matches(httpServletRequest);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenWhitelistedGlobalMask() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("8.8.8.8");

        var privateNetworkMatcher = new WhitelistIpRequestMatcher(Set.of("0.0.0.0/0"));
        final boolean result = privateNetworkMatcher.matches(httpServletRequest);
        assertThat(result).isTrue();
    }


}
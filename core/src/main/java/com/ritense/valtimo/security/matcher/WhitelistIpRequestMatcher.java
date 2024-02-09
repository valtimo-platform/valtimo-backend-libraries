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

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class WhitelistIpRequestMatcher implements RequestMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistIpRequestMatcher.class);
    private final RequestMatcher requestMatcher;

    public WhitelistIpRequestMatcher(Collection<String> hosts) {
        if (hosts == null) {
            requestMatcher = denyMatcher();
        } else {
            List<RequestMatcher> ipAddressMatchers = hosts.stream()
                .filter(host -> host != null && !host.isBlank())
                .flatMap(host -> {
                    if (host.contains("/")) {
                        return Stream.of(host);
                    } else {
                        try {
                            return Arrays.stream(InetAddress.getAllByName(host)).map(InetAddress::getHostAddress);
                        } catch (Exception e) {
                            LOGGER.warn("Could not resolve whitelisted host {}", host);
                        }
                        return Stream.of();
                    }
                })
                .map(IpAddressMatcher::new)
                .collect(Collectors.toList());

            if (ipAddressMatchers.isEmpty()) {
                requestMatcher = denyMatcher();
            } else {
                requestMatcher = new OrRequestMatcher(ipAddressMatchers);
            }
        }
    }

    public RequestMatcher denyMatcher() {
        return request -> false;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if (request.getRemoteAddr().isBlank()) {
            return false;
        }
        return requestMatcher.matches(request);
    }
}
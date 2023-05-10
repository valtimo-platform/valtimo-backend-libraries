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

package com.ritense.valtimo.security.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class WhitelistIpRequest implements RequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistIpRequest.class);
    private final Set<String> hostAddresses;

    public WhitelistIpRequest(Collection<String> hosts) {
        this.hostAddresses = resolveHosts(hosts);
    }

    private static Set<String> resolveHosts(Collection<String> hosts) {
        if (hosts != null) {
            return hosts.stream()
                .filter(host -> host != null && !host.isBlank())
                .flatMap(host -> {
                    try {
                        return Arrays.stream(InetAddress.getAllByName(host)).map(InetAddress::getHostAddress);
                    } catch (Exception e) {
                        LOGGER.warn("Could not resolve host " + host, e);
                    }
                    return null;
                })
                .filter(host -> host != null && !host.isBlank())
                .collect(Collectors.toSet());
        }

        return Set.of();
    }

    public boolean check(HttpServletRequest httpServletRequest) {
        return hostAddresses.contains(httpServletRequest.getRemoteAddr());
    }
}
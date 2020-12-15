/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.common.util.IpUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class OfficeIpRequest implements RequestInterceptor {

    private static final String KANTOOR_DOMEIN = "kantoor.ritense.com";
    private final String hostAddress;

    public OfficeIpRequest() {
        try {
            hostAddress = InetAddress.getByName(KANTOOR_DOMEIN).getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to get kantoor domain hostaddress", e);
        }
    }

    public boolean check(HttpServletRequest httpServletRequest) {
        return IpUtils.extractSourceIpsFrom(httpServletRequest)
            .stream()
            .anyMatch(ip -> ip.equals(hostAddress));
    }

}
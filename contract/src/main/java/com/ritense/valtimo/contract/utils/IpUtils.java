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

package com.ritense.valtimo.contract.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IpUtils {

    static final String X_FORWARDED_FOR = "X-Forwarded-For";

    public static Set<String> extractSourceIpsFrom(HttpServletRequest request) {
        final String header = request.getHeader(X_FORWARDED_FOR);
        Set<String> ipList = new HashSet<>();
        if (header != null) {
            final String[] xForwardIps = header.split(",");
            ipList.addAll(Arrays.asList(xForwardIps));
        }
        final String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null) {
            ipList.add(remoteAddr);
        }
        return ipList;
    }

}
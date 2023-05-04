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

package com.ritense.formlink.web.rest.impl.interceptor;

import com.ritense.valtimo.contract.utils.RequestHelper;
import io.github.bucket4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated(since = "10.6.0", forRemoval = true)
public class PublicAccessRateLimitInterceptor implements HandlerInterceptor {

    private static final ConcurrentHashMap<String, Bucket> ipMap = new ConcurrentHashMap<>();
    private static final Bandwidth publicAccessBandwidth = Bandwidth
        .classic(60, Refill.intervally(60, Duration.ofMinutes(1)));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final Bucket ipTokenBucket = ipMap.computeIfAbsent(RequestHelper.getOrigin(), s -> Bucket4j.builder()
            .addLimit(publicAccessBandwidth)
            .build()
        );

        ConsumptionProbe probe = ipTokenBucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "You have exhausted your API Request Quota");
            return false;
        }
    }

}

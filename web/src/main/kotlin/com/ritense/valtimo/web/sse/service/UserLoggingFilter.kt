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

package com.ritense.valtimo.web.sse.service

import com.ritense.valtimo.contract.authentication.UserManagementService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

class UserLoggingFilter(
    private val userManagementService: UserManagementService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userIdentifier = userManagementService.getCurrentUser().getUserIdentifier()
        if (userIdentifier != null) {
            MDC.put(USER_MDC_KEY, userIdentifier)
        }

        // We also add a correlation id manually for now.
        MDC.put(CORRELATION_MDC_KEY, UUID.randomUUID().toString())

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(USER_MDC_KEY)
            MDC.remove(CORRELATION_MDC_KEY)
        }
    }

    companion object {
        private const val USER_MDC_KEY = "userId"
        private const val CORRELATION_MDC_KEY = "correlationId"
    }
}
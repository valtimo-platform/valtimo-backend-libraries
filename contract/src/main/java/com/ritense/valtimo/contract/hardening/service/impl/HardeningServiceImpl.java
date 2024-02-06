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

package com.ritense.valtimo.contract.hardening.service.impl;

import com.ritense.valtimo.contract.hardening.config.HardeningProperties;
import com.ritense.valtimo.contract.hardening.domain.SourceIpList;
import com.ritense.valtimo.contract.hardening.service.HardeningService;
import com.ritense.valtimo.contract.hardening.throwable.SanitizedThrowable;
import com.ritense.valtimo.contract.hardening.throwable.UnsanitizedThrowable;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.ThrowableProblem;

public class HardeningServiceImpl implements HardeningService {
    private static final Logger logger = LoggerFactory.getLogger(HardeningServiceImpl.class);
    private final HardeningProperties hardeningProperties;
    private static final String WHITELIST_MESSSAGE = "IP address is whitelisted: ";

    public HardeningServiceImpl(HardeningProperties hardeningProperties) {
        this.hardeningProperties = hardeningProperties;
    }

    public Throwable harden(Throwable ex, HttpServletRequest request) {
        final Set<String> whitelists = hardeningProperties.getAllowStacktraceOnIps();
        final SourceIpList sourceIpList = new SourceIpList(whitelists, Set.of(request.getRemoteAddr()));

        if (sourceIpList.isWhitelisted()) {
            return UnsanitizedThrowable.withReason(ex, WHITELIST_MESSSAGE + sourceIpList.getWhiteListedIp());
        } else {
            return eraseStacktrace(ex);
        }
    }

    @Override
    public ProblemBuilder harden(ThrowableProblem throwableProblem, ProblemBuilder problemBuilder, HttpServletRequest request) {
        final Set<String> whitelists = hardeningProperties.getAllowStacktraceOnIps();
        final SourceIpList sourceIpList = new SourceIpList(whitelists, Set.of(request.getRemoteAddr()));
        if (sourceIpList.isWhitelisted()) {
            return problemBuilder
                .with("reason-not-sanitized", WHITELIST_MESSSAGE + sourceIpList.getWhiteListedIp())
                .with("stack-trace", throwableProblem.getStackTrace())
                .withDetail(throwableProblem.getDetail())
                .withCause(throwableProblem.getCause());
        } else {
            UUID referenceId = UUID.randomUUID();
            logger.info("Exception occurred with reference id {}", referenceId, throwableProblem);
            return problemBuilder
                .with("referenceId", referenceId);
        }
    }

    /**
     * Returns a new instance of a Throwable containing only the message of the given exception.
     * Also recursively does the same for any "causes", if the cause exception object is not a self-reference (that is a thing in Java, yes)
     *
     * @param ex The exception that is going to be wrapped
     * @return A proxy of the given exception that doesn't return stacktrace information
     */
    protected <E extends Throwable> SanitizedThrowable eraseStacktrace(E ex) {
        // possible corner-case: given null, return null
        if (ex == null) {
            return null;
        }

        boolean exHasProperCause = ex.getCause() != null && !ex.getCause().equals(ex);
        String exCanonicalName = ex.getClass().getCanonicalName();
        String exMessage = ex.getMessage();

        SanitizedThrowable sanitizedThrowable;
        if (exHasProperCause) {
            SanitizedThrowable sanitizedCause = eraseStacktrace(ex.getCause());
            sanitizedThrowable = SanitizedThrowable.withCause(exCanonicalName, exMessage, sanitizedCause);
        } else {
            // No proper cause
            sanitizedThrowable = SanitizedThrowable.withoutCause(exCanonicalName, exMessage);
        }
        return sanitizedThrowable;
    }

}
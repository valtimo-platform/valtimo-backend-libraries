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

package com.ritense.logging

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.hardening.service.HardeningService
import com.ritense.valtimo.contract.web.rest.error.ExceptionTranslator
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import java.util.Optional

@SkipComponentScan
@ControllerAdvice
class LoggingContextExceptionTranslator(
    hardeningService: HardeningService?
) : ExceptionTranslator(Optional.ofNullable(hardeningService)) {

    override fun log(throwable: Throwable, problem: Problem, request: NativeWebRequest, status: HttpStatus) {
        withErrorLoggingContext {
            super.log(throwable, problem, request, status)
        }
    }
}

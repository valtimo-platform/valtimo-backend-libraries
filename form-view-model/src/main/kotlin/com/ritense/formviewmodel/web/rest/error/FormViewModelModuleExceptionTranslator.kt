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

package com.ritense.formviewmodel.web.rest.error

import com.ritense.formviewmodel.error.BusinessException
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.web.rest.dto.BusinessRuleError
import com.ritense.formviewmodel.web.rest.dto.FormError
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class FormViewModelModuleExceptionTranslator : ProblemHandling {

    @ExceptionHandler(FormException::class)
    fun handleFormException(
        ex: FormException,
        request: NativeWebRequest
    ): ResponseEntity<FormError> {
        return ResponseEntity
            .badRequest()
            .body(
                FormError.Builder()
                    .setError(ex.message)
                    .setComponent(ex.component)
                    .build()
            )
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: NativeWebRequest
    ): ResponseEntity<BusinessRuleError> {
        return ResponseEntity
            .badRequest()
            .body(
                BusinessRuleError.Builder()
                    .setError(ex.message)
                    .build()
            )
    }

}
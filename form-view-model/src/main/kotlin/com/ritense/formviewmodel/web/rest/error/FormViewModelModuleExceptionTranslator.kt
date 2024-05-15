package com.ritense.formviewmodel.web.rest.error

import com.ritense.formviewmodel.error.FormException
import com.ritense.valtimo.web.rest.error.ErrorConstants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class FormViewModelModuleExceptionTranslator : ProblemHandling {

    @ExceptionHandler
    fun handleFormException(ex: FormException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withStatus(defaultConstraintViolationStatus())
            .withTitle("Form exception")
            .withDetail(ex.message)
            .apply {
                ex.component?.let { with("component", it) }
            }
            .build()
        return create(ex, problem, request)
    }

}
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
    fun handleFormException(ex: FormException, request: NativeWebRequest): ResponseEntity<FormError> {
        return ResponseEntity
            .badRequest()
            .body(FormError(error = ex.message!!, component = ex.component))
    }


    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: FormException, request: NativeWebRequest): ResponseEntity<BusinessRuleError> {
        return ResponseEntity
            .badRequest()
            .body(BusinessRuleError(error = ex.message!!))
    }

}
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

package com.ritense.document.web.rest.error;

import com.ritense.valtimo.contract.hardening.service.HardeningService;
import com.ritense.valtimo.contract.web.rest.error.ExceptionTranslator;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import javax.validation.ValidationException;
import java.util.Optional;

@ControllerAdvice
public class DocumentModuleExceptionTranslator extends ExceptionTranslator implements ProblemHandling {

    public DocumentModuleExceptionTranslator(Optional<HardeningService> hardeningServiceOptional) {
        super(hardeningServiceOptional);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleValidationException(ValidationException ex, NativeWebRequest request) {
        return create(Status.BAD_REQUEST, ex, request, HeaderUtil.createFailureAlert(ex.getMessage(), "validationException", ex.getMessage()));
    }

}

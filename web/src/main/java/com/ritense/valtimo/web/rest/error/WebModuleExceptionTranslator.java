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

package com.ritense.valtimo.web.rest.error;

import com.ritense.valtimo.contract.exception.DocumentParserException;
import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import com.ritense.valtimo.contract.exception.ValtimoRuntimeException;
import com.ritense.valtimo.contract.hardening.service.HardeningService;
import com.ritense.valtimo.contract.upload.MimeTypeDeniedException;
import com.ritense.valtimo.contract.web.rest.error.ExceptionTranslator;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 */
@ControllerAdvice
public class WebModuleExceptionTranslator extends ExceptionTranslator implements ProblemHandling {

    public WebModuleExceptionTranslator(Optional<HardeningService> hardeningServiceOptional) {
        super(hardeningServiceOptional);
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors()
            .stream()
            .map(f -> new FieldErrorVM(f.getObjectName(), f.getField(), f.getCode()))
            .collect(Collectors.toList());

        Problem problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with("message", ErrorConstants.ERR_VALIDATION)
            .with("fieldErrors", fieldErrors)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleNoSuchElementException(NoSuchElementException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.NOT_FOUND)
            .with("message", ErrorConstants.ENTITY_NOT_FOUND_TYPE)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleBadRequestAlertException(BadRequestAlertException ex, NativeWebRequest request) {
        return create(ex, request, HeaderUtil.createFailureAlert(ex.getEntityName(), ex.getErrorKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with("message", ErrorConstants.ERR_CONCURRENCY_FAILURE)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleEntityException(EntityException ex, NativeWebRequest request) {
        return create(Status.BAD_REQUEST, ex, request, HeaderUtil.createFailureAlert(ex.getEntityName(), ex.getTranslationKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleProcessNotFoundException(ProcessNotFoundException ex, NativeWebRequest request) {
        return create(Status.BAD_REQUEST, ex, request, HeaderUtil.createFailureAlert(ex.getMessage(), "processNotFound", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleDocumentParserException(DocumentParserException ex, NativeWebRequest request) {
        return create(Status.BAD_REQUEST, ex, request, HeaderUtil.createFailureAlert(ex.getMessage(), "parsingFailure", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleMimeTypeDeniedException(MimeTypeDeniedException ex, NativeWebRequest request) {
        return create(Status.BAD_REQUEST, ex, request, HeaderUtil.createFailureAlert(ex.getMessage(), "mimeTypeDenied", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleAccessDenied(AccessDeniedException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.FORBIDDEN)
            .with("message", ErrorConstants.ERR_ACCESS_DENIED)
            .withDetail(ex.getMessage())
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.METHOD_NOT_ALLOWED)
            .with("message", ErrorConstants.ERR_METHOD_NOT_SUPPORTED)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleValtimoRuntimeException(ValtimoRuntimeException ex, NativeWebRequest request) {
        return create(Status.BAD_REQUEST, ex, request, HeaderUtil.createFailureAlert(ex.getMessage(), ex.getCategory(), ex.getErrorDescription()));
    }

}

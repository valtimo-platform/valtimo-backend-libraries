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

package com.ritense.valtimo.web.rest.error;

import com.ritense.valtimo.contract.exception.DocumentParserException;
import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CamundaExceptionTranslator {

    @ExceptionHandler(ProcessNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleProcessNotFoundException(ProcessNotFoundException ex) {
        return ResponseEntity.badRequest()
            .headers(HeaderUtil.createFailureAlert(ex.getMessage(), "processNotFound", ex.getMessage()))
            .body(null);
    }

    @ExceptionHandler(DocumentParserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleDocumentParserException(DocumentParserException ex) {
        return ResponseEntity.badRequest()
            .headers(HeaderUtil.createFailureAlert(ex.getMessage(), "parsingFailure", ex.getMessage()))
            .body(null);
    }

    @ExceptionHandler(FormFieldValidatorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<FieldErrorVM> handleFormFieldValidationException(FormFieldValidatorException ex) {
        return ResponseEntity.badRequest().body(new FieldErrorVM(null, ex.getName(), ex.getId()));
    }
}

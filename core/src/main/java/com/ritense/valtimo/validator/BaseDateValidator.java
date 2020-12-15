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

package com.ritense.valtimo.validator;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Slf4j
public abstract class BaseDateValidator {
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    protected Optional<DateValidatorDTO> validateDates(Object submittedValue, FormFieldValidatorContext validatorContext) {
        Optional<DateValidatorDTO> dateValidatorDTO = Optional.empty();

        Date submittedValueDate = null;
        try {
            submittedValueDate = dateFormat.parse(submittedValue.toString());
        } catch (ParseException e) {
            logger.error("The submitted value is not a valid date with format: {}", DATE_FORMAT);
        }
        Date configValue = null;
        try {
            configValue = dateFormat.parse(validatorContext.getConfiguration());
        } catch (ParseException e) {
            logger.error("Invalid date set in Camunda Modeler. Please make sure the format is {}", DATE_FORMAT);
        }

        if (configValue != null && submittedValueDate != null) {
            dateValidatorDTO = Optional.of(new DateValidatorDTO(submittedValueDate, configValue));
        }
        return dateValidatorDTO;
    }
}

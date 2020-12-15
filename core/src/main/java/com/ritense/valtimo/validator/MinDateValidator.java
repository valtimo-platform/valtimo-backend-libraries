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

import org.camunda.bpm.engine.impl.form.validator.FormFieldValidator;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorContext;

import java.util.Optional;

public class MinDateValidator extends BaseDateValidator implements FormFieldValidator {

    @Override
    public boolean validate(Object submittedValue, FormFieldValidatorContext validatorContext) {
        Optional<DateValidatorDTO> dateValidatorDTOOptional = super.validateDates(submittedValue, validatorContext);
        if (dateValidatorDTOOptional.isPresent()) {
            DateValidatorDTO dateValidatorDTO = dateValidatorDTOOptional.get();
            return dateValidatorDTO.getSubmittedValue().after(dateValidatorDTO.getConfigValue())
                ||
                dateValidatorDTO.getSubmittedValue().equals(dateValidatorDTO.getConfigValue());
        }
        return false;
    }
}

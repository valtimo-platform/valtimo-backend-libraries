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
package com.ritense.form.validation

import com.ritense.form.service.FormDefinitionService
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.UUID

class FormDefinitionExistsValidator(
    private val formDefinitionService: FormDefinitionService
) : ConstraintValidator<FormDefinitionExists, Any> {
    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }

        return when (value) {
            is String -> {
                try {
                    formDefinitionService.formDefinitionExistsById(UUID.fromString(value))
                } catch (e: IllegalArgumentException) {
                    formDefinitionService.getFormDefinitionByName(value).isPresent
                }
            }

            is UUID -> {
                formDefinitionService.formDefinitionExistsById(value)
            }

            else -> {
                throw UnsupportedOperationException("Value of type ${value.javaClass.name} is not supported")
            }
        }
    }

}
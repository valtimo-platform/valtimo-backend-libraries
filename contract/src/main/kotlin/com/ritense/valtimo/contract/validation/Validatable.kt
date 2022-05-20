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

package com.ritense.valtimo.contract.validation

import com.fasterxml.jackson.annotation.JsonIgnore
import mu.KotlinLogging
import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator

interface Validatable {

    @JsonIgnore
    fun validate() {
        val logger = KotlinLogging.logger {}
        logger.debug { "validating $this" }
        val errors = getValidator().validate(this)
        if (errors.isNotEmpty()) {
            throw ConstraintViolationException(errors)
        }
    }

    companion object {
        private var validator: Validator? = null

        fun getValidator(): Validator {
            return validator ?: Validation.buildDefaultValidatorFactory().validator
        }

        fun setValidator(validator: Validator) {
            Validatable.validator = validator
        }
    }

}

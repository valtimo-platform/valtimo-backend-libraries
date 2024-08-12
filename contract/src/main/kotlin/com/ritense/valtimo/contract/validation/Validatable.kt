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

package com.ritense.valtimo.contract.validation

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Validator
import mu.KLogger
import mu.KotlinLogging

interface Validatable {

    @JsonIgnore
    fun validate() {
        logger.debug { "validating $this" }
        ValidatorHolder.validate(this)
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}

        @Deprecated("This method has been moved to ValidatorHolder", ReplaceWith("ValidatorHolder.getValidator()"))
        fun getValidator(): Validator {
            return ValidatorHolder.getValidator()
        }

        @Deprecated("This method has been moved to ValidatorHolder", ReplaceWith("ValidatorHolder.setValidator(validator)"))
        fun setValidator(validator: Validator) {
            ValidatorHolder.setValidator(validator)
        }
    }

}

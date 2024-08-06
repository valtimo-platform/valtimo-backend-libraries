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

import jakarta.validation.Validation
import jakarta.validation.Validator

class ValidatorHolder {

    companion object {
        private lateinit var validator: Validator

        @JvmStatic
        fun validate(obj:Any) {
            getValidator().check(obj)
        }

        @JvmStatic
        fun getValidator(): Validator {
            return if (this::validator.isInitialized) {
                validator
            } else Validation.buildDefaultValidatorFactory().validator
        }

        fun setValidator(validator: Validator) {
            this.validator = validator
        }
    }
}
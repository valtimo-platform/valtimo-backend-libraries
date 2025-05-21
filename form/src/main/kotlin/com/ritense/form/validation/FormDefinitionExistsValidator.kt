/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

import com.ritense.form.exception.FormDefinitionNotFoundException
import com.ritense.form.service.FormDefinitionService
import com.ritense.valtimo.contract.case_.CaseDefinitionId

class FormDefinitionExistsValidator(
    private val formDefinitionService: FormDefinitionService
) {
    init {
        Companion.formDefinitionService = formDefinitionService
    }

    companion object {
        private lateinit var formDefinitionService: FormDefinitionService

        @JvmStatic
        fun isValid(formDefinitionName: String, caseDefinitionId: CaseDefinitionId?) {
            if (!formDefinitionService.getFormDefinitionByName(formDefinitionName, caseDefinitionId).isPresent)
                throw FormDefinitionNotFoundException(formDefinitionName, caseDefinitionId)
        }
    }
}
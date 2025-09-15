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

package com.ritense.case.service.exception

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.valtimo.web.rest.error.BadRequestAlertException

import org.zalando.problem.Exceptional

class ExportLimitExceedsException(caseDefinitionKey: String) : BadRequestAlertException
    ("Export failed for case '$caseDefinitionKey': the number of cases exceeds the maximum limit of 10,000. Please refine your search criteria.",
    JsonSchemaDocument::class.simpleName,
    "exportLimit"
) {
    override fun getCause(): Exceptional? {
        return null
    }
}
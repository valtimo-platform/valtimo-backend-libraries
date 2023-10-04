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

package com.ritense.case.repository

import com.ritense.case.domain.CaseTab
import org.springframework.data.jpa.domain.Specification

class CaseTabSpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val CASE_DEFINITION_NAME: String = "caseDefinitionName"
        const val KEY: String = "key"
        const val TAB_ORDER: String = "tabOrder"

        @JvmStatic
        fun byCaseDefinitionName(caseDefinitionName: String) = Specification<CaseTab> { root, _, cb ->
            cb.equal(root.get<Any>(ID).get<Any>(CASE_DEFINITION_NAME), caseDefinitionName)
        }

        @JvmStatic
        fun byCaseDefinitionNameAndTabKey(caseDefinitionName: String, tabKey: String) =
            byCaseDefinitionName(caseDefinitionName).and { root, _, cb ->
                cb.equal(root.get<Any>(ID).get<Any>(KEY), tabKey)
            }
    }
}
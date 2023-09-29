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

package com.ritense.case.service

import com.ritense.case.domain.CaseTab
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.TAB_ORDER
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionName
import org.springframework.data.domain.Sort

class CaseTabService(
    private val caseTabRepository: CaseTabRepository
) {

    fun getCaseTabs(caseDefinitionName: String): List<CaseTab> {
        return caseTabRepository.findAll(byCaseDefinitionName(caseDefinitionName), Sort.by(TAB_ORDER))
    }

}

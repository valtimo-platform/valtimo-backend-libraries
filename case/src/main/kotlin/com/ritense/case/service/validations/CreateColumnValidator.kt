/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.case.service.validations

import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valueresolver.ValueResolverService

class CreateColumnValidator(
    override val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    override val documentDefinitionService: DocumentDefinitionService,
    override val valueResolverService: ValueResolverService,
) : ValidationUtils(
    caseDefinitionListColumnRepository,
    documentDefinitionService,
    valueResolverService
), CaseDefinitionColumnValidator {

    @Throws(InvalidListColumnException::class)
    override fun validate(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        existsDocumentDefinition(caseDefinitionName)
        existsListColumn(caseDefinitionName, caseListColumnDto)
        isCreateColumnDefaultSortValid(caseDefinitionName, caseListColumnDto)
        isPropertyPathValid(caseDefinitionName, caseListColumnDto)
        caseListColumnDto.validate()
    }

    override fun validate(caseDefinitionName: String, caseListColumnDtoList: List<CaseListColumnDto>) {
        TODO("Not yet implemented")
    }
}

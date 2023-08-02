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

package com.ritense.case.service.validations

import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valueresolver.ValueResolverService
import org.zalando.problem.Status

class UpdateColumnValidator(
    caseDefinitionSettingsRepository: CaseDefinitionListColumnRepository,
    documentDefinitionService: DocumentDefinitionService<JsonSchemaDocumentDefinition>,
    valueResolverService: ValueResolverService,
) : ValidationUtils(
    caseDefinitionSettingsRepository,
    documentDefinitionService,
    valueResolverService
), CaseDefinitionColumnValidator {
    override fun validate(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        TODO("Not yet implemented")
    }


    override fun validate(caseDefinitionName: String, caseListColumnDtoList: List<CaseListColumnDto>) {
        existsDocumentDefinition(caseDefinitionName)
        val columns =
            caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(caseDefinitionName)
        val defaultSortColumns =
            caseListColumnDtoList.filter { caseListColumnDto -> caseListColumnDto.defaultSort != null }
        if (defaultSortColumns.size > 1) {
            throw InvalidListColumnException(
                "Invalid set of columns. There is more than 1 column with default sort value",
                Status.BAD_REQUEST
            )
        }
        overrideListColumnDtoWithDefaultSort(caseDefinitionName, caseListColumnDtoList, columns)
        caseListColumnDtoList.forEach { caseListColumnDto ->
            isPropertyPathValid(caseDefinitionName, caseListColumnDto)
            caseListColumnDto.validate()
        }
    }
}

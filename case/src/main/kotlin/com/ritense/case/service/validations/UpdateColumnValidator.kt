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
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.document.service.DocumentDefinitionService
import org.zalando.problem.Status

class UpdateColumnValidator(
    caseDefinitionSettingsRepository: CaseDefinitionListColumnRepository,
    documentDefinitionService: DocumentDefinitionService
) : ValidationUtils(caseDefinitionSettingsRepository, documentDefinitionService), CaseDefinitionColumnValidator {

    @Throws(InvalidListColumnException::class)
    override fun validate(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        existsDocumentDefinition(caseDefinitionName)
        existsListColumn(caseDefinitionName, caseListColumnDto)
        isJsonPathValid(caseDefinitionName, caseListColumnDto)
        caseListColumnDto.validate()
    }

    override fun validate(caseDefinitionName: String, caseListColumnDtoList: List<CaseListColumnDto>) {
        val columns = caseDefinitionListColumnRepository.findByIdCaseDefinitionName(caseDefinitionName)
        val defaultSortColumns =
            caseListColumnDtoList.filter { caseListColumnDto -> caseListColumnDto.defaultSort != null }
        if (defaultSortColumns.size > 1) {
            throw InvalidListColumnException(
                "Invalid set of columns. There is more than 1 column with default sort value",
                Status.BAD_REQUEST
            )
        }
        caseListColumnDtoList.forEach { caseListColumnDto ->
            if (existsColumnWithDefaultSort(caseListColumnDto, columns)) {
                val column = CaseListColumnMapper.toDto(columns.first { column -> column.defaultSort != null })
                column.defaultSort = null
                caseDefinitionListColumnRepository.save(CaseListColumnMapper.toEntity(caseDefinitionName, column))
            }
        }

    }
}
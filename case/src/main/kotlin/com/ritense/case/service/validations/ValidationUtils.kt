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

import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valueresolver.ValueResolverService
import org.zalando.problem.Status

open class ValidationUtils(
    open val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    open val documentDefinitionService: DocumentDefinitionService,
    open val valueResolverService: ValueResolverService,
) {

    @Throws(InvalidListColumnException::class)
    internal fun existsListColumn(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        if (caseDefinitionListColumnRepository.existsByIdCaseDefinitionNameAndIdKey(
                caseDefinitionName,
                caseListColumnDto.key
            )
        ) {
            throw InvalidListColumnException(
                "Unable to create list column. A column with the same key already exists",
                Status.BAD_REQUEST
            )
        }
    }

    @Throws(InvalidListColumnException::class)
    internal fun isCreateColumnDefaultSortValid(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        if (existsColumnWithDefaultSort(
                caseListColumnDto,
                caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
                    caseDefinitionName
                )
            )
        ) {
            throw InvalidListColumnException(
                "Unable to create list column. A column with defaultSort value already exists",
                Status.BAD_REQUEST
            )
        }
    }

    @Throws(InvalidListColumnException::class)
    internal fun existsDocumentDefinition(documentDefinitionName: String) {
        try {
            //TODO: Fix pbac
            runWithoutAuthorization {
                documentDefinitionService.findIdByName(documentDefinitionName)
            }
        } catch (ex: UnknownDocumentDefinitionException) {
            throw UnknownCaseDefinitionException(ex.message)
        }
    }

    @Throws
    internal fun overrideListColumnDtoWithDefaultSort(
        caseDefinitionName: String,
        caseListColumnDtoList: List<CaseListColumnDto>,
        columns: List<CaseListColumn>
    ) {
        caseListColumnDtoList.forEach { caseListColumnDto ->
            if (existsColumnWithDefaultSort(caseListColumnDto, columns)) {
                val column = CaseListColumnMapper.toDto(columns.first { column -> column.defaultSort != null })
                column.defaultSort = null
                caseDefinitionListColumnRepository.save(CaseListColumnMapper.toEntity(caseDefinitionName, column))
            }
        }
    }

    internal fun existsColumnWithDefaultSort(
        caseListColumnDto: CaseListColumnDto, columns: List<CaseListColumn>
    ): Boolean {
        return caseListColumnDto.defaultSort != null &&
                columns.any { column -> column.defaultSort != null }
    }

    @Throws(InvalidListColumnException::class)
    internal fun isPropertyPathValid(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        try {
            valueResolverService.validateValues(caseDefinitionName, listOf(caseListColumnDto.path))
        } catch (ex: Exception) {
            throw InvalidListColumnException(ex.message, Status.BAD_REQUEST)
        }
    }

}

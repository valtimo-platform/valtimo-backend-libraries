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

package com.ritense.case.web.rest.mapper

import com.ritense.case.domain.CaseListColumn
import com.ritense.case.domain.CaseListColumnId
import com.ritense.case.web.rest.dto.CaseListColumnDto

class CaseListColumnMapper {
    companion object {

        fun toEntity(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto): CaseListColumn {
            return CaseListColumn(
                CaseListColumnId(caseDefinitionName, caseListColumnDto.key),
                caseListColumnDto.title,
                caseListColumnDto.path,
                caseListColumnDto.displayType,
                caseListColumnDto.sortable,
                caseListColumnDto.defaultSort
            )
        }

        fun toDto(caseListColumn: CaseListColumn): CaseListColumnDto {
            return CaseListColumnDto(
                caseListColumn.title,
                caseListColumn.id.key,
                caseListColumn.path,
                caseListColumn.displayType,
                caseListColumn.sortable,
                caseListColumn.defaultSort
            )
        }

        fun toEntityList(caseDefinitionName: String, caseListColumns: List<CaseListColumnDto>): List<CaseListColumn> {
            return caseListColumns.stream().map { column ->
                toEntity(caseDefinitionName, column)
            }.toList()
        }

        fun toDtoList(caseListColumns: List<CaseListColumn>): List<CaseListColumnDto> {
            return caseListColumns.stream().map { column ->
                toDto(column)
            }.toList()
        }
    }
}
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

package com.ritense.case.service.validations

import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.case.web.rest.dto.TaskListColumnDto
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valueresolver.ValueResolverService

class SaveTaskListColumnValidator(
    override val taskListColumnRepository: TaskListColumnRepository,
    override val documentDefinitionService: DocumentDefinitionService,
    override val valueResolverService: ValueResolverService,
) : TaskListColumnValidationUtils(
    taskListColumnRepository,
    documentDefinitionService,
    valueResolverService
), ListColumnValidator<TaskListColumnDto> {

    @Throws(InvalidListColumnException::class)
    override fun validate(caseDefinitionName: String, caseListColumnDto: TaskListColumnDto) {
        assertDocumentDefinitionExists(caseDefinitionName)
        isColumnDefaultSortValid(caseDefinitionName, caseListColumnDto)
        isPropertyPathValid(caseDefinitionName, caseListColumnDto)
        caseListColumnDto.validate()
    }

    override fun validate(caseDefinitionName: String, caseListColumnDtoList: List<TaskListColumnDto>) {
        TODO("Not yet implemented")
    }
}

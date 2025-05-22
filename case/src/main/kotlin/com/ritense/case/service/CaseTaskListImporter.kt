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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.web.rest.dto.TaskListColumnDto
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_TASK_LIST
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.logging.withLoggingContext
import com.ritense.valtimo.contract.case_.CaseDefinitionId

class CaseTaskListImporter(
    private val objectMapper: ObjectMapper,
    private val taskColumnService: TaskColumnService,
) : Importer {
    override fun type(): String = CASE_TASK_LIST

    override fun dependsOn(): Set<String> = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String): Boolean = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        deploy(request.caseDefinitionId!!, request.content.toString(Charsets.UTF_8))
    }

    private fun deploy(caseDefinitionId: CaseDefinitionId, content: String) {
        val columns = getJson(content)

        runWithoutAuthorization {
            withLoggingContext("jsonSchemaDocumentName" to caseDefinitionId.key) {
                columns.map { taskListColumnDto ->
                    taskColumnService.saveListColumn(caseDefinitionId.key, taskListColumnDto)
                }
            }
        }
    }

    private fun getJson(rawJson: String): List<TaskListColumnDto> {
        return objectMapper.readValue<List<TaskListColumnDto>>(rawJson)
    }

    private companion object {
        val FILENAME_REGEX = """/case/task-list/([^/]+)\.case-task-list\.json""".toRegex()
    }
}
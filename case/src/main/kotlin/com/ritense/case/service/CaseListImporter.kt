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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_LIST
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.logging.withLoggingContext
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseListImporter(
    private val resourcePatternResolver: ResourcePatternResolver,
    private val objectMapper: ObjectMapper,
    private val caseDefinitionService: CaseDefinitionService
) : Importer {
    override fun type() = CASE_LIST

    override fun dependsOn() = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val caseDefinitionName = FILENAME_REGEX.matchEntire(request.fileName)!!.groupValues[1]
        withLoggingContext("jsonSchemaDocumentName" to caseDefinitionName) {
            deployColumns(caseDefinitionName, request.content.toString(Charsets.UTF_8))
        }
    }

    fun deployColumns(caseDefinitionName: String, jsonContent: String) {
        validate(jsonContent)

        val existingColumns = caseDefinitionService.getListColumns(caseDefinitionName)

        val formFlowDefinitionConfig = objectMapper.readValue(jsonContent,
            object : TypeReference<List<CaseListColumnDto>>() {})

        caseDefinitionService.updateListColumns(caseDefinitionName, formFlowDefinitionConfig)

        val keysToLoad = formFlowDefinitionConfig.map { it.key }
        val columnsToDelete = existingColumns.filterNot { keysToLoad.contains(it.key) }

        columnsToDelete.forEach {
            caseDefinitionService.deleteCaseListColumn(caseDefinitionName, it.key)
        }
    }

    private fun validate(json: String) {
        val definitionJsonObject = JSONArray(JSONTokener(json))

        val schema = SchemaLoader.load(JSONObject(JSONTokener(loadCaseListSchemaResource().inputStream)))
        schema.validate(definitionJsonObject)
    }

    private fun loadCaseListSchemaResource(): Resource {
        return resourcePatternResolver.getResource(CASE_LIST_SCHEMA_PATH)
    }

    private companion object {
        const val CASE_LIST_SCHEMA_PATH = "classpath:config/case/schema/case-list.schema.json"
        val FILENAME_REGEX = """/case/list/([^/]+)\.json""".toRegex()
    }
}
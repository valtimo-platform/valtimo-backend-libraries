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

package com.ritense.case.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.web.rest.dto.CaseListColumnDto
import mu.KotlinLogging
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

open class CaseListDeploymentService(
    private val resourcePatternResolver: ResourcePatternResolver,
    private val objectMapper: ObjectMapper,
    private val caseDefinitionService: CaseDefinitionService
) {

    @Transactional
    @EventListener(ApplicationReadyEvent::class)
    open fun deployColumns() {
        logger.info("Deploying case list column definitions")
        try {
            val resources = loadCaseListResources()
            resources.forEach { resource ->
                if (resource.filename != null) {
                    deployColumnsForCase(resource)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Error deploying case list column definitions", e)
        }
    }

    private fun deployColumnsForCase(resource: Resource) {
        val caseDefinitionName = resource.filename!!.substringBeforeLast(".")
        val caseColumnsJson = StreamUtils.copyToString(resource.inputStream, StandardCharsets.UTF_8)

        validate(caseColumnsJson)

        val existingColumns = caseDefinitionService.getListColumns(caseDefinitionName)

        val formFlowDefinitionConfig = objectMapper.readValue(caseColumnsJson,
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

    private fun loadCaseListResources(): Array<Resource> {
        return resourcePatternResolver.getResources(CASE_LIST_DEFINITIONS_PATH)
    }

    private fun loadCaseListSchemaResource(): Resource {
        return resourcePatternResolver.getResource(CASE_LIST_SCHEMA_PATH)
    }

    companion object {
        internal const val CASE_LIST_SCHEMA_PATH = "classpath:config/case/schema/case-list.schema.json"
        internal const val CASE_LIST_DEFINITIONS_PATH = "classpath:config/case/list/*.json"
        val logger = KotlinLogging.logger {}
    }
}

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

import CaseDefinitionDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull

class CaseDefinitionImporter(
    private val objectMapper: ObjectMapper,
    private val caseDefinitionRepository: CaseDefinitionRepository
) : Importer {
    override fun type() = CASE_DEFINITION

    override fun dependsOn() = setOf<String>()

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        deploy(request.content.toString(Charsets.UTF_8), true)
    }

    private fun deploy(fileContent: String, forceDeploy: Boolean = false) {
        val caseDefinitionDto = try {
            objectMapper.readValue(fileContent, CaseDefinitionDto::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse file content as a valid case definition: ${e.message}", e)
        }

        val caseDefinition = caseDefinitionDto.toEntity()

        logger.debug { "Deploying case definition with id '${caseDefinition.id}'" }

        val existingCaseDefinition = caseDefinitionRepository.findByIdOrNull(caseDefinition.id)

        if (existingCaseDefinition == null || forceDeploy) { // TODO: revisit forceDeploy this when doing drafts
            caseDefinitionRepository.save(caseDefinition)
            logger.debug { "Case definition with id '${caseDefinition.id}' was saved" }
        } else {
            logger.debug { "Not deploying case definition with '${caseDefinition.id}', it already exists" }
        }
    }

    private companion object {
        val logger = KotlinLogging.logger {}
        val FILENAME_REGEX = """/case/definition/([^/]+)\.json""".toRegex()
    }
}
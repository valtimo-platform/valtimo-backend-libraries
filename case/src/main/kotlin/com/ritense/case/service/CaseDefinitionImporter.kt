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
import io.github.oshai.kotlinlogging.KotlinLogging
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker

class CaseDefinitionImporter(
    private val objectMapper: ObjectMapper,
    private val caseDefinitionRepository: CaseDefinitionRepository,
    private val caseDefinitionChecker: CaseDefinitionChecker,
) : Importer {
    override fun type() = CASE_DEFINITION

    override fun dependsOn() = setOf<String>()

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        deploy(request.content.toString(Charsets.UTF_8))
    }

    override fun afterImport(request: ImportRequest) {
        val caseDefinitionDto = toCaseDefinitionDto(request.content.toString(Charsets.UTF_8))
        if (caseDefinitionDto.final) {
            caseDefinitionRepository.save(caseDefinitionDto.toEntity())
        }
    }

    private fun deploy(fileContent: String) {
        val caseDefinitionDto = toCaseDefinitionDto(fileContent)
        val caseDefinitionId = caseDefinitionDto.getCaseDefinitionId()

        caseDefinitionChecker.assertCanCreateOrUpdateCaseDefinition(caseDefinitionId, caseDefinitionDto.final)

        val caseDefinition = caseDefinitionDto.toEntity().copy(final = false)

        logger.debug { "Deploying case definition with id '${caseDefinition.id}'" }

        caseDefinitionRepository.save(caseDefinition)
        logger.debug { "Case definition with id '${caseDefinition.id}' was saved" }
    }

    private fun toCaseDefinitionDto(fileContent: String): CaseDefinitionDto {
        return try {
            objectMapper.readValue(fileContent, CaseDefinitionDto::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse file content as a valid case definition: ${e.message}", e)
        }
    }

    private companion object {
        val logger = KotlinLogging.logger {}
        val FILENAME_REGEX = """/case/definition/([^/]+)\.case-definition\.json""".toRegex()
    }
}
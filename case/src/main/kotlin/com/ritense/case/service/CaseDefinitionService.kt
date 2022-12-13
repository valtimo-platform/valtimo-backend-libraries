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

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import org.zalando.problem.Status

class CaseDefinitionService(
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository,
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService
) {
    @Throws(UnknownDocumentDefinitionException::class)
    fun getCaseSettings(caseDefinitionName: String): CaseDefinitionSettings {
        checkIfDocumentDefinitionExists(caseDefinitionName)
        return caseDefinitionSettingsRepository.getById(caseDefinitionName)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun updateCaseSettings(caseDefinitionName: String, newSettings: CaseSettingsDto): CaseDefinitionSettings {
        checkIfDocumentDefinitionExists(caseDefinitionName)
        val caseDefinitionSettings = caseDefinitionSettingsRepository.getById(caseDefinitionName)
        val updatedCaseDefinition = newSettings.update(caseDefinitionSettings)
        return caseDefinitionSettingsRepository.save(updatedCaseDefinition)
    }

    @Throws(InvalidListColumnException::class)
    fun createListColumn(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        validateListColumn(caseDefinitionName, caseListColumnDto)
        caseDefinitionListColumnRepository.save(CaseListColumnMapper.toEntity(caseDefinitionName, caseListColumnDto))
    }

    @Throws(InvalidListColumnException::class, UnknownDocumentDefinitionException::class)
    private fun validateListColumn(caseDefinitionName: String, caseListColumnDto: CaseListColumnDto) {
        try {
            checkIfDocumentDefinitionExists(caseDefinitionName)
        } catch (ex: UnknownDocumentDefinitionException) {
            throw InvalidListColumnException(ex.message, Status.BAD_REQUEST)
        }
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
        if (
            caseListColumnDto.defaultSort != null &&
            caseDefinitionListColumnRepository.findByIdCaseDefinitionName(caseDefinitionName)
                .any { column -> column.defaultSort != null }
        ) {
            throw InvalidListColumnException(
                "Unable to create list column. A column with defaultSort value already exists",
                Status.BAD_REQUEST
            )
        }
        try {
            documentDefinitionService.validateJsonPath(caseDefinitionName, caseListColumnDto.path)
        } catch (ex: Exception) {
            throw InvalidListColumnException(ex.message, Status.BAD_REQUEST)
        }
        caseListColumnDto.validate(caseDefinitionName)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    private fun checkIfDocumentDefinitionExists(caseDefinitionName: String) {
        documentDefinitionService.findIdByName(caseDefinitionName)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun getListColumns(caseDefinitionName: String): List<CaseListColumnDto> {
        try {
            checkIfDocumentDefinitionExists(caseDefinitionName)
        } catch (ex: UnknownDocumentDefinitionException) {
            throw UnknownCaseDefinitionException(ex.message)
        }
        return CaseListColumnMapper
            .toDtoList(caseDefinitionListColumnRepository.findByIdCaseDefinitionName(caseDefinitionName))
    }
}
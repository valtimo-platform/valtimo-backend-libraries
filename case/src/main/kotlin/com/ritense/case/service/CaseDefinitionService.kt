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
import com.ritense.case.service.validations.CaseDefinitionColumnValidator
import com.ritense.case.service.validations.CreateColumnValidator
import com.ritense.case.service.validations.Operation
import com.ritense.case.service.validations.UpdateColumnValidator
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import org.springframework.transaction.annotation.Transactional

open class CaseDefinitionService(
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository,
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService
) {
    var validators: Map<Operation, CaseDefinitionColumnValidator> = mapOf(
        Pair(
            Operation.CREATE, CreateColumnValidator(caseDefinitionListColumnRepository, documentDefinitionService)
        ),
        Pair(
            Operation.UPDATE, UpdateColumnValidator(caseDefinitionListColumnRepository, documentDefinitionService)
        )
    )

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

    @Transactional
    @Throws(InvalidListColumnException::class)
    open fun upsertListColumn(
        caseDefinitionName: String,
        caseListColumnDtoList: List<CaseListColumnDto>,
        operation: Operation
    ) {
        when (operation) {
            Operation.CREATE -> {
                validators[operation]!!.validate(caseDefinitionName, caseListColumnDtoList[0])
                caseDefinitionListColumnRepository
                    .save(CaseListColumnMapper.toEntity(caseDefinitionName, caseListColumnDtoList[0]))
            }

            Operation.UPDATE -> {
                validators[operation]!!.validate(caseDefinitionName, caseListColumnDtoList)
                caseDefinitionListColumnRepository
                    .saveAll(CaseListColumnMapper.toEntityList(caseDefinitionName, caseListColumnDtoList))
            }
        }
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
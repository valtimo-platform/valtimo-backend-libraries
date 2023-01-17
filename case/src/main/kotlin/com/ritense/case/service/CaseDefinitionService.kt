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
import com.ritense.valueresolver.ValueResolverService
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseDefinitionService(
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository,
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    private val valueResolverService: ValueResolverService,
) {
    var validators: Map<Operation, CaseDefinitionColumnValidator> = mapOf(
        Operation.CREATE to CreateColumnValidator(
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            valueResolverService
        ),
        Operation.UPDATE to UpdateColumnValidator(
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            valueResolverService
        )
    )

    @Throws(UnknownDocumentDefinitionException::class)
    fun getCaseSettings(caseDefinitionName: String): CaseDefinitionSettings {
        checkIfDocumentDefinitionExists(caseDefinitionName)
        return caseDefinitionSettingsRepository.getReferenceById(caseDefinitionName)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun updateCaseSettings(caseDefinitionName: String, newSettings: CaseSettingsDto): CaseDefinitionSettings {
        checkIfDocumentDefinitionExists(caseDefinitionName)
        val caseDefinitionSettings = caseDefinitionSettingsRepository.getReferenceById(caseDefinitionName)
        val updatedCaseDefinition = newSettings.update(caseDefinitionSettings)
        return caseDefinitionSettingsRepository.save(updatedCaseDefinition)
    }

    @Throws(InvalidListColumnException::class)
    fun createListColumn(
        caseDefinitionName: String,
        caseListColumnDto: CaseListColumnDto
    ) {
        validators[Operation.CREATE]!!.validate(caseDefinitionName, caseListColumnDto)
        caseListColumnDto.order = caseDefinitionListColumnRepository.countByIdCaseDefinitionName(caseDefinitionName)
        caseDefinitionListColumnRepository
            .save(CaseListColumnMapper.toEntity(caseDefinitionName, caseListColumnDto))
    }

    fun updateListColumns(
        caseDefinitionName: String,
        caseListColumnDtoList: List<CaseListColumnDto>
    ) {

        validators[Operation.UPDATE]!!.validate(caseDefinitionName, caseListColumnDtoList)
        var order = 0
        caseListColumnDtoList.forEach { caseListColumnDto ->
            caseListColumnDto.order = order++
        }
        caseDefinitionListColumnRepository
            .saveAll(CaseListColumnMapper.toEntityList(caseDefinitionName, caseListColumnDtoList))
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
            .toDtoList(
                caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
                    caseDefinitionName
                )
            )
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun deleteCaseListColumn(caseDefinitionName: String, columnKey: String) {
        try {
            checkIfDocumentDefinitionExists(caseDefinitionName)
        } catch (ex: UnknownDocumentDefinitionException) {
            throw UnknownCaseDefinitionException(ex.message)
        }
        if (caseDefinitionListColumnRepository
                .existsByIdCaseDefinitionNameAndIdKey(caseDefinitionName, columnKey)
        ) {
            caseDefinitionListColumnRepository.deleteByIdCaseDefinitionNameAndIdKey(caseDefinitionName, columnKey)
        }
    }
}

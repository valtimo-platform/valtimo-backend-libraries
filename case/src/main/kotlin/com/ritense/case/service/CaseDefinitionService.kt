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

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.service.validations.CreateCaseListColumnValidator
import com.ritense.case.service.validations.ListColumnValidator
import com.ritense.case.service.validations.Operation
import com.ritense.case.service.validations.UpdateCaseListColumnValidator
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.ValueResolverService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Transactional
@Service
@SkipComponentScan
class CaseDefinitionService(
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    private val caseDefinitionRepository: CaseDefinitionRepository,
    valueResolverService: ValueResolverService,
    private val authorizationService: AuthorizationService
) {
    var validators: Map<Operation, ListColumnValidator<CaseListColumnDto>> = mapOf(
        Operation.CREATE to CreateCaseListColumnValidator(
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            valueResolverService
        ),
        Operation.UPDATE to UpdateCaseListColumnValidator(
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            valueResolverService
        )
    )

    fun getCaseDefinitions(pageable: Pageable): Page<CaseDefinition> {
        return caseDefinitionRepository.findAllLatestCaseDefinitions(pageable)
    }

    fun getCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition {
        return caseDefinitionRepository.findByIdOrNull(caseDefinitionId)
            ?: throw UnknownCaseDefinitionException(caseDefinitionId)
    }

    fun getCaseDefinitionVersions(caseDefinitionKey: String): List<String> {
        return caseDefinitionRepository.findVersionsForCaseDefinitionKey(caseDefinitionKey).map {
            it.toString()
        }
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun updateCaseSettings(caseDefinitionId: CaseDefinitionId, newSettings: CaseSettingsDto): CaseDefinition {
        denyManagementOperation()

        val caseDefinition = newSettings.update(
            runWithoutAuthorization { getCaseDefinition(caseDefinitionId) }
        )

        return caseDefinitionRepository.save(caseDefinition)
    }

    @Throws(InvalidListColumnException::class)
    fun createListColumn(
        caseDefinitionKey: String,
        caseListColumnDto: CaseListColumnDto
    ) {
        denyManagementOperation()

        runWithoutAuthorization {
            validators[Operation.CREATE]!!.validate(caseDefinitionKey, caseListColumnDto)
        }
        caseListColumnDto.order = caseDefinitionListColumnRepository.countByIdCaseDefinitionKey(caseDefinitionKey)
        caseDefinitionListColumnRepository
            .save(CaseListColumnMapper.toEntity(caseDefinitionKey, caseListColumnDto))
    }

    fun updateListColumns(
        caseDefinitionName: String,
        caseListColumnDtoList: List<CaseListColumnDto>
    ) {
        denyManagementOperation()

        runWithoutAuthorization {
            validators[Operation.UPDATE]!!.validate(caseDefinitionName, caseListColumnDtoList)
        }
        var order = 0
        caseListColumnDtoList.forEach { caseListColumnDto ->
            caseListColumnDto.order = order++
        }
        caseDefinitionListColumnRepository
            .saveAll(CaseListColumnMapper.toEntityList(caseDefinitionName, caseListColumnDtoList))
    }


    @Throws(UnknownDocumentDefinitionException::class)
    fun getListColumns(caseDefinitionKey: String): List<CaseListColumnDto> {
        // TODO: Implement PBAC:
        // It currently relies on the VIEW check in findLatestByName via assertDocumentDefinitionExists.
        // Doing a check here forces this class to be a JsonSchemaDocument implementation, which is undesirable.
        assertDocumentDefinitionExists(caseDefinitionKey)

        return CaseListColumnMapper
            .toDtoList(
                caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(
                    caseDefinitionKey
                )
            )
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun deleteCaseListColumn(caseDefinitionKey: String, columnKey: String) {
        denyManagementOperation()

        runWithoutAuthorization { assertDocumentDefinitionExists(caseDefinitionKey) }

        if (caseDefinitionListColumnRepository
                .existsByIdCaseDefinitionKeyAndIdKey(caseDefinitionKey, columnKey)
        ) {
            caseDefinitionListColumnRepository.deleteByIdCaseDefinitionKeyAndIdKey(caseDefinitionKey, columnKey)
        }
    }

    private fun denyManagementOperation() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                Any::class.java,
                Action.deny()
            )
        )
    }

    internal fun getLatestCaseDefinition(caseDefinitionKey: String): CaseDefinition? {
        return caseDefinitionRepository.findFirstByIdKeyOrderByIdVersionTagDesc(caseDefinitionKey)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    private fun assertDocumentDefinitionExists(caseDefinitionKey: String): DocumentDefinition {
        return documentDefinitionService.findLatestByName(caseDefinitionKey)
            .getOrNull() ?: throw UnknownCaseDefinitionException(caseDefinitionKey)
    }
}

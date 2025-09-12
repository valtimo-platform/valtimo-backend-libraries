/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.service.validations.CreateCaseListColumnValidator
import com.ritense.case.service.validations.ListColumnValidator
import com.ritense.case.service.validations.Operation
import com.ritense.case.service.validations.UpdateCaseListColumnValidator
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.valueresolver.ValueResolverService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Transactional
@Service
@SkipComponentScan
class CaseDefinitionService(
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository,
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService,
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

    @Throws(UnknownDocumentDefinitionException::class)
    fun getCaseSettings(caseDefinitionName: String): CaseDefinitionSettings {
        // TODO: Implement PBAC:
        // It currently relies on the VIEW check in findLatestByName via assertDocumentDefinitionExists.
        // Doing a check here forces this class to be a JsonSchemaDocument implementation, which is undesirable.
        assertDocumentDefinitionExists(caseDefinitionName)

        return caseDefinitionSettingsRepository.getReferenceById(caseDefinitionName)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun updateCaseSettings(caseDefinitionName: String, newSettings: CaseSettingsDto): CaseDefinitionSettings {
        denyManagementOperation()

        runWithoutAuthorization { assertDocumentDefinitionExists(caseDefinitionName) }
        val caseDefinitionSettings = caseDefinitionSettingsRepository.getReferenceById(caseDefinitionName)
        val updatedCaseDefinition = newSettings.update(caseDefinitionSettings)
        return caseDefinitionSettingsRepository.save(updatedCaseDefinition)
    }

    @Throws(InvalidListColumnException::class)
    fun createListColumn(
        caseDefinitionName: String,
        caseListColumnDto: CaseListColumnDto
    ) {
        denyManagementOperation()

        runWithoutAuthorization {
            validators[Operation.CREATE]!!.validate(caseDefinitionName, caseListColumnDto)
        }
        caseListColumnDto.order = caseDefinitionListColumnRepository.countByIdCaseDefinitionName(caseDefinitionName)

        if (caseListColumnDto.exportable) {
            validateExportPath(caseListColumnDto.path, caseListColumnDto.key)
        }

        caseDefinitionListColumnRepository
            .save(CaseListColumnMapper.toEntity(caseDefinitionName, caseListColumnDto))

        logger.info { "User '${getCurrentUser()}' created a case list column configuration: '$caseListColumnDto' for case definition: '$caseDefinitionName'"}
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

        caseListColumnDtoList
            .filter { it.exportable }
            .forEach { dto ->
                validateExportPath(dto.path, dto.key)
            }

        val entities = CaseListColumnMapper.toEntityList(caseDefinitionName, caseListColumnDtoList)

        caseDefinitionListColumnRepository
            .saveAll(entities)

        val currentUser = getCurrentUser()

        if(currentUser != null) {
            logger.info { "User '${currentUser}' " +
                "updated case list column configuration: '$entities' for case definition: '$caseDefinitionName'"}
        }
    }


    @Throws(UnknownDocumentDefinitionException::class)
    fun getListColumns(caseDefinitionName: String): List<CaseListColumnDto> {
        // TODO: Implement PBAC:
        // It currently relies on the VIEW check in findLatestByName via assertDocumentDefinitionExists.
        // Doing a check here forces this class to be a JsonSchemaDocument implementation, which is undesirable.
        assertDocumentDefinitionExists(caseDefinitionName)

        return CaseListColumnMapper
            .toDtoList(
                caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
                    caseDefinitionName
                )
            )
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun deleteCaseListColumn(caseDefinitionName: String, columnKey: String) {
        denyManagementOperation()

        runWithoutAuthorization { assertDocumentDefinitionExists(caseDefinitionName) }

        if (caseDefinitionListColumnRepository
                .existsByIdCaseDefinitionNameAndIdKey(caseDefinitionName, columnKey)
        ) {
            caseDefinitionListColumnRepository.deleteByIdCaseDefinitionNameAndIdKey(caseDefinitionName, columnKey)
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

    @Throws(UnknownDocumentDefinitionException::class)
    private fun assertDocumentDefinitionExists(caseDefinitionName: String): DocumentDefinition {
        return documentDefinitionService.findLatestByName(caseDefinitionName)
            .getOrNull() ?: throw UnknownCaseDefinitionException(caseDefinitionName)
    }

    private fun validateExportPath(path: String, key: String) {
        require(PATH_REGEX_EXPORTABLE.containsMatchIn(path)) {
            "Failed to save the case list column configuration for key '$key'. Only document or case properties can be exported."
        }
    }

    private fun getCurrentUser(): String? {
        return SecurityUtils.getCurrentUserLogin()
    }

    companion object {
        val logger = KotlinLogging.logger {}
        val PATH_REGEX_EXPORTABLE = Regex("^(case:|doc:)")
    }
}

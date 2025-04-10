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
import com.ritense.case.web.rest.dto.CaseDefinitionDraftCreateRequest
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
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.valueresolver.ValueResolverService
import mu.KotlinLogging
import org.semver4j.Semver
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Transactional
@Service
@SkipComponentScan
class CaseDefinitionService(
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    private val caseDefinitionRepository: CaseDefinitionRepository,
    valueResolverService: ValueResolverService,
    private val authorizationService: AuthorizationService,
    private val applicationEventPublisher: ApplicationEventPublisher,
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

    fun createCaseDefinitionDraft(
        basedOnCaseDefinitionId: CaseDefinitionId,
        request: CaseDefinitionDraftCreateRequest
    ): CaseDefinition {
        denyManagementOperation()
        val newCaseDefinitionId = CaseDefinitionId.of(basedOnCaseDefinitionId.key, request.versionTag)
        require(!caseDefinitionRepository.existsById(newCaseDefinitionId)) {
            "Failed to create case-definition-draft. Case-definition with id: '$newCaseDefinitionId' already exists."
        }
        val basedOnCaseDefinition = getCaseDefinition(basedOnCaseDefinitionId)
        require(basedOnCaseDefinition.final) {
            "Failed to create case-definition-draft. Case-definition with id: '$basedOnCaseDefinitionId' is not final."
        }
        val newCaseDefinition = caseDefinitionRepository.save(
            basedOnCaseDefinition.copy(
                id = newCaseDefinitionId,
                description = request.description,
                final = false,
                createdBy = SecurityUtils.getCurrentUserLogin(),
                createdDate = LocalDateTime.now(),
                basedOnVersionTag = basedOnCaseDefinitionId.versionTag,
                active = false
            )
        )
        applicationEventPublisher.publishEvent(
            CaseDefinitionCreatedEvent(newCaseDefinitionId, basedOnCaseDefinitionId)
        )
        return newCaseDefinition
    }

    fun deleteCaseDefinition(caseDefinitionId: CaseDefinitionId) {
        denyManagementOperation()
        require(!getCaseDefinition(caseDefinitionId).active) {
            "Failed to delete case-definition. Case-definition with id: '$caseDefinitionId' is the global active version."
        }
        require(!getCaseDefinition(caseDefinitionId).final) {
            "Failed to delete case-definition. Case-definition with id: '$caseDefinitionId' is final."
        }
        applicationEventPublisher.publishEvent(
            CaseDefinitionPreDeleteEvent(caseDefinitionId)
        )
        caseDefinitionRepository.deleteById(caseDefinitionId)
    }

    fun getCaseDefinitions(pageable: Pageable): Page<CaseDefinition> {
        return caseDefinitionRepository.findAllByActiveIsTrue(pageable)
    }

    fun getCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition {
        return caseDefinitionRepository.findByIdOrNull(caseDefinitionId)
            ?: throw UnknownCaseDefinitionException(caseDefinitionId)
    }

    fun findCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition? {
        return caseDefinitionRepository.findByIdOrNull(caseDefinitionId)
    }

    fun getActiveCaseDefinition(caseDefinitionKey: String): CaseDefinition? {
        val caseDefinitions = caseDefinitionRepository.findAllByIdKeyOrderByIdVersionTagDesc(caseDefinitionKey)
        if (caseDefinitions.isEmpty()) return null
        val activeCaseDefinitions = caseDefinitions.filter {it.active }
        require (activeCaseDefinitions.size <= 1) {
            "Multiple active case-definitions found for case-definition '$caseDefinitionKey'"
        }
        if (activeCaseDefinitions.isEmpty()) {
            // TODO: throw error here
            logger.error { "No active case-definition found for case-definition '$caseDefinitionKey'" }
        }
        return activeCaseDefinitions.firstOrNull() ?: caseDefinitions.firstOrNull()
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun setActiveCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition {
        denyManagementOperation()
        val caseDefinition = runWithoutAuthorization { getCaseDefinition(caseDefinitionId) }

        val activeCaseDefinition = caseDefinitionRepository.findByActiveIsTrueAndIdKey(caseDefinitionId.key)
        if (activeCaseDefinition != null && activeCaseDefinition.id != caseDefinitionId) {
            caseDefinitionRepository.save(activeCaseDefinition.copy(active = false))
        }

        return caseDefinitionRepository.save(caseDefinition.copy(active = true))
    }

    fun getCaseDefinitionVersions(caseDefinitionKey: String): List<String> {
        return caseDefinitionRepository.findVersionsForCaseDefinitionKey(caseDefinitionKey).map {
            it.toString()
        }
    }

    fun getCaseDefinitionsBasedOnVersion(caseDefinitionKey: String, basedOnVersionTag: Semver): List<CaseDefinition> {
        return caseDefinitionRepository.findAllByIdKeyAndBasedOnVersionTag(caseDefinitionKey, basedOnVersionTag)
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

    @Throws(UnknownDocumentDefinitionException::class)
    private fun assertDocumentDefinitionExists(caseDefinitionKey: String): DocumentDefinition {
        return documentDefinitionService.findLatestByName(caseDefinitionKey)
            .getOrNull() ?: throw UnknownCaseDefinitionException(caseDefinitionKey)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

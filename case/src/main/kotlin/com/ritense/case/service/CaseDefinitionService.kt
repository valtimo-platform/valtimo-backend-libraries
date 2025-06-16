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
import com.ritense.case.repository.CaseDefinitionSpecificationHelper.Companion.byActive
import com.ritense.case.repository.CaseDefinitionSpecificationHelper.Companion.byCaseDefinitionKey
import com.ritense.case.repository.CaseDefinitionSpecificationHelper.Companion.byCaseDefinitionVersionTag
import com.ritense.case.repository.CaseDefinitionSpecificationHelper.Companion.byFinal
import com.ritense.case.service.validations.CreateCaseListColumnValidator
import com.ritense.case.service.validations.ListColumnValidator
import com.ritense.case.service.validations.Operation
import com.ritense.case.service.validations.UpdateCaseListColumnValidator
import com.ritense.case.web.rest.dto.CaseDefinitionDraftCreateRequest
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.case_.authorization.CaseDefinitionActionProvider
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.valueresolver.ValueResolverService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.semver4j.Semver
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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
    private val caseDefinitionChecker: CaseDefinitionChecker,
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
        request: CaseDefinitionDraftCreateRequest
    ): CaseDefinition {
        denyManagementOperation()
        val caseDefinitionId = request.getCaseDefinitionId()
        require(!caseDefinitionRepository.existsById(caseDefinitionId)) {
            "Failed to create case-definition-draft. Case-definition with id: '${caseDefinitionId}' already exists."
        }
        val basedOnCaseDefinitionId = request.getBasedOnCaseDefinitionId()
        val newCaseDefinition = if (basedOnCaseDefinitionId == null) {
            CaseDefinition(
                id = caseDefinitionId,
                name = request.name ?: error("Case definition name cannot be null"),
                description = request.description,
                final = false,
                createdBy = SecurityUtils.getCurrentUserLogin(),
                createdDate = LocalDateTime.now(),
                active = true
            )
        } else {
            val basedOnCaseDefinition = getCaseDefinition(basedOnCaseDefinitionId)
            require(basedOnCaseDefinition.final) {
                "Failed to create case-definition-draft. Case-definition with id: '$basedOnCaseDefinitionId' is not final."
            }
            basedOnCaseDefinition.copy(
                id = caseDefinitionId,
                description = request.description ?: basedOnCaseDefinition.description,
                final = false,
                createdBy = SecurityUtils.getCurrentUserLogin(),
                createdDate = LocalDateTime.now(),
                basedOnVersionTag = basedOnCaseDefinitionId.versionTag,
                active = false
            )
        }
        val newSavedCaseDefinition = caseDefinitionRepository.save(newCaseDefinition)
        caseDefinitionChecker.assertCanUpdateCaseDefinition(newSavedCaseDefinition.id)
        applicationEventPublisher.publishEvent(
            CaseDefinitionCreatedEvent(
                caseDefinitionId = newSavedCaseDefinition.id,
                caseDefinitionName = newSavedCaseDefinition.name,
                basedOnCaseDefinitionId = basedOnCaseDefinitionId,
                duplicate = basedOnCaseDefinitionId != null,
                copyFormDefinitionsAfterProcessLinks = true
            )
        )
        return newSavedCaseDefinition
    }

    fun deleteCaseDefinition(caseDefinitionId: CaseDefinitionId) {
        denyManagementOperation()
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)
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

    fun existsCaseDefinition(caseDefinitionKey: String): Boolean {
        return caseDefinitionRepository.existsByIdKey(caseDefinitionKey)
    }

    fun getCaseDefinitions(
        caseDefinitionKey: String? = null,
        caseDefinitionVersionTag: Semver? = null,
        active: Boolean? = null,
        final: Boolean? = null,
        pageable: Pageable,
    ): Page<CaseDefinition> {
        if (active == null || !active) {
            denyManagementOperation()
        }
        val spec = getCaseDefinitionsQuery(
            caseDefinitionKey = caseDefinitionKey,
            caseDefinitionVersionTag = caseDefinitionVersionTag,
            active = active,
            final = final,
        )
        return caseDefinitionRepository.findAll(spec, pageable)
    }

    fun getCaseDefinitions(
        caseDefinitionKey: String? = null,
        caseDefinitionVersionTag: Semver? = null,
        active: Boolean? = null,
        final: Boolean? = null,
    ): List<CaseDefinition> {
        if (active == null || !active) {
            denyManagementOperation()
        }

        val spec =
            getCaseDefinitionsQuery(
                caseDefinitionKey = caseDefinitionKey,
                caseDefinitionVersionTag = caseDefinitionVersionTag,
                active = active,
                final = final,
            )
        return caseDefinitionRepository.findAll(spec)
    }

    fun getCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition {
        return caseDefinitionRepository.findByIdOrNull(caseDefinitionId)
            ?: throw UnknownCaseDefinitionException(caseDefinitionId)
    }

    fun findCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition? {
        return caseDefinitionRepository.findByIdOrNull(caseDefinitionId)
    }

    fun getActiveCaseDefinition(caseDefinitionKey: String): CaseDefinition? {
        return caseDefinitionRepository.findByActiveIsTrueAndIdKey(caseDefinitionKey)
    }

    fun updateCaseDefinition(caseDefinitionId: CaseDefinitionId, name: String?, description: String?): CaseDefinition {
        denyManagementOperation()
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)
        val caseDefinition = getCaseDefinition(caseDefinitionId)
        return caseDefinitionRepository.save(
            caseDefinition.copy(
                name = name ?: caseDefinition.name,
                description = description ?: caseDefinition.description
            )
        )
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

    fun finalizeCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition {
        denyManagementOperation()
        val caseDefinition = getCaseDefinition(caseDefinitionId)
        require(!caseDefinition.final) {
            "Failed to finalize case-definition. Case-definition with id: '$caseDefinitionId' is already final."
        }
        return caseDefinitionRepository.save(caseDefinition.copy(final = true))
    }

    fun getCaseDefinitionsBasedOnVersion(caseDefinitionKey: String, basedOnVersionTag: Semver): List<CaseDefinition> {
        return caseDefinitionRepository.findAllByIdKeyAndBasedOnVersionTag(caseDefinitionKey, basedOnVersionTag)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun updateCaseSettings(caseDefinitionId: CaseDefinitionId, newSettings: CaseSettingsDto): CaseDefinition {
        denyManagementOperation()
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)

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
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseDefinition::class.java,
                CaseDefinitionActionProvider.VIEW,
                runWithoutAuthorization {
                    getCaseDefinitions(
                        caseDefinitionKey = caseDefinitionKey,
                        active = true
                    )
                }
            )
        )

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

    fun setLatestToActiveIfNoneIsActive() {
        caseDefinitionRepository.findAll()
            .groupBy { it.id.key }
            .map { it.value }
            .filter { caseDefinitions -> caseDefinitions.none { caseDefinition -> caseDefinition.active } }
            .map { caseDefinitions -> caseDefinitions.maxBy { it.id.versionTag } }
            .map { caseDefinition -> caseDefinition.copy(active = true) }
            .forEach { caseDefinition -> caseDefinitionRepository.save(caseDefinition) }
    }

    private fun getCaseDefinitionsQuery(
        caseDefinitionKey: String? = null,
        caseDefinitionVersionTag: Semver? = null,
        active: Boolean? = null,
        final: Boolean? = null,
    ): Specification<CaseDefinition> {
        var spec: Specification<CaseDefinition> = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                CaseDefinition::class.java,
                CaseDefinitionActionProvider.VIEW_LIST
            )
        )

        if (caseDefinitionKey != null) {
            spec = spec.and(byCaseDefinitionKey(caseDefinitionKey))
        }
        if (caseDefinitionVersionTag != null) {
            spec = spec.and(byCaseDefinitionVersionTag(caseDefinitionVersionTag))
        }
        if (active != null) {
            spec = spec.and(byActive(active))
        }
        if (final != null) {
            spec = spec.and(byFinal(final))
        }
        return spec
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
    private fun assertDocumentDefinitionExists(caseDefinitionKey: String) {
        if (!documentDefinitionService.existsByName(caseDefinitionKey)) {
            throw UnknownCaseDefinitionException(caseDefinitionKey)
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

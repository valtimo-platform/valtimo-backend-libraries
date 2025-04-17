package com.ritense.document.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.service.CaseDefinitionService
import com.ritense.document.domain.CaseTag
import com.ritense.document.domain.CaseTagId
import com.ritense.document.exception.CaseTagAlreadyExistsException
import com.ritense.document.exception.CaseTagInUseException
import com.ritense.document.exception.CaseTagNotFoundException
import com.ritense.document.repository.CaseTagRepository
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.validation.Valid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Validated
@Transactional
@Service
@SkipComponentScan
class CaseTagService(
    private val caseTagRepository: CaseTagRepository,
    private val caseDefinitionService: CaseDefinitionService,
    private val authorizationService: AuthorizationService
) {

    fun getCaseTags(caseDefinitionId: CaseDefinitionId): List<CaseTag> {
        return caseTagRepository.findByIdCaseDefinitionIdOrderByOrder(caseDefinitionId)
    }

    fun get(caseDefinitionId: CaseDefinitionId, caseTagKey: String): CaseTag {
        return caseTagRepository.getReferenceById(CaseTagId(caseDefinitionId, caseTagKey))
    }

    fun exists(caseDefinitionId: CaseDefinitionId, caseTagKey: String): Boolean {
        return caseTagRepository.existsByIdCaseDefinitionIdAndIdKey(caseDefinitionId, caseTagKey)
    }

    fun create(
        caseDefinitionId: CaseDefinitionId,
        @Valid request: CaseTagCreateRequestDto
    ): CaseTag {
        denyManagementOperation()

        caseDefinitionService.findCaseDefinition(caseDefinitionId)
            ?: throw NoSuchElementException("Case definition${caseDefinitionId} does not exist!")

        val currentCaseTags = getCaseTags(caseDefinitionId)
        if (currentCaseTags.any { status ->
                status.id.key == request.key
            }) {
            throw CaseTagAlreadyExistsException(request.key, caseDefinitionId)
        }

        return caseTagRepository.save(
            CaseTag(
                CaseTagId(
                    caseDefinitionId,
                    request.key
                ),
                request.title,
                request.color,
                order = currentCaseTags.size
            )
        )
    }


    fun update(
        caseDefinitionId: CaseDefinitionId,
        caseTagKey: String,
        @Valid request: CaseTagUpdateRequestDto,
    ) {
        denyManagementOperation()

        val oldCaseTag = caseTagRepository
            .findDistinctByIdCaseDefinitionIdAndIdKey(
                caseDefinitionId, caseTagKey
            ) ?: throw CaseTagNotFoundException(caseTagKey, caseDefinitionId)

        caseTagRepository.save(
            oldCaseTag.copy(
                title = request.title,
                color = request.color
            )
        )
    }

    fun update(
        caseDefinitionId: CaseDefinitionId,
        @Valid requests: List<CaseTagUpdateRequestDto>
    ): List<CaseTag> {
        denyManagementOperation()

        val existingCaseTags = caseTagRepository
            .findByIdCaseDefinitionIdOrderByOrder(caseDefinitionId)
        check(existingCaseTags.size == requests.size) {
            throw IllegalStateException(
                "Failed to update case tags. Reason: the number of "
                    + "case tags in the update request does not match the number of existing case tags."
            )
        }

        val updatedCaseTags = requests.mapIndexed { index, request ->
            val existingCaseTag = existingCaseTags.find { it.id.key == request.key }
                ?: throw CaseTagNotFoundException(request.key, caseDefinitionId)
            existingCaseTag.copy(
                title = request.title,
                color = request.color,
                order = index
            )
        }

        return caseTagRepository.saveAll(updatedCaseTags)
    }

    fun delete(caseDefinitionId: CaseDefinitionId, caseTagKey: String) {
        denyManagementOperation()

        val caseTag =
            caseTagRepository.findDistinctByIdCaseDefinitionIdAndIdKey(
                caseDefinitionId, caseTagKey
            ) ?: throw CaseTagNotFoundException(caseTagKey, caseDefinitionId)

        if (caseTagRepository.isCaseTagInUse(caseTagKey, caseDefinitionId.key, caseDefinitionId.versionTag)) {
            throw CaseTagInUseException(caseTagKey, caseDefinitionId)
        }

        caseTagRepository.delete(caseTag)
        reorder(caseDefinitionId)
    }

    private fun reorder(caseDefinitionId: CaseDefinitionId) {
        val caseTags = caseTagRepository.findByIdCaseDefinitionIdOrderByOrder(
            caseDefinitionId
        ).mapIndexed { index, caseTag -> caseTag.copy(order = index) }
        caseTagRepository.saveAll(caseTags)
    }

    private fun denyManagementOperation() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                Any::class.java,
                Action.deny()
            )
        )
    }

}
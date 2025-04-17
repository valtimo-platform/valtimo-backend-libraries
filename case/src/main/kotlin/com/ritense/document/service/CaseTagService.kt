package com.ritense.document.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.CaseTag
import com.ritense.document.domain.CaseTagId
import com.ritense.document.exception.CaseTagAlreadyExistsException
import com.ritense.document.exception.CaseTagInUseException
import com.ritense.document.exception.CaseTagNotFoundException
import com.ritense.document.repository.CaseTagRepository
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import jakarta.validation.Valid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import kotlin.jvm.optionals.getOrNull

@Validated
@Transactional
@Service
@SkipComponentScan
class CaseTagService(
    private val caseTagRepository: CaseTagRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    private val authorizationService: AuthorizationService
) {

    fun getCaseTags(documentDefinitionName: String): List<CaseTag> {
        return caseTagRepository.findByIdCaseDefinitionNameOrderByOrder(documentDefinitionName)
    }

    fun get(caseDefinitionName: String, caseTagKey: String): CaseTag {
        return caseTagRepository.getReferenceById(CaseTagId(caseDefinitionName, caseTagKey))
    }

    fun exists(caseDefinitionName: String, caseTagKey: String): Boolean {
        return caseTagRepository.existsByIdCaseDefinitionNameAndIdKey(caseDefinitionName, caseTagKey)
    }

    fun create(
        caseDefinitionName: String,
        @Valid request: CaseTagCreateRequestDto
    ) : CaseTag {
        denyManagementOperation()

        documentDefinitionService.findLatestByName(caseDefinitionName).getOrNull()
            ?: throw NoSuchElementException("Case definition with name $caseDefinitionName does not exist!")

        val currentCaseTags = getCaseTags(caseDefinitionName)
        if (currentCaseTags.any { status ->
                status.id.key == request.key
            }) {
            throw CaseTagAlreadyExistsException(request.key, caseDefinitionName)
        }

        return caseTagRepository.save(
            CaseTag(
                CaseTagId(
                    caseDefinitionName,
                    request.key
                ),
                request.title,
                request.color,
                order = currentCaseTags.size
            )
        )
    }


    fun update(
        caseDefinitionName: String,
        caseTagKey: String,
        @Valid request: CaseTagUpdateRequestDto,
    ) {
        denyManagementOperation()

        val  oldCaseTag= caseTagRepository
            .findDistinctByIdCaseDefinitionNameAndIdKey(
                caseDefinitionName, caseTagKey
            ) ?: throw CaseTagNotFoundException(caseTagKey, caseDefinitionName)

        caseTagRepository.save(
            oldCaseTag.copy(
                title = request.title,
                color = request.color
            )
        )
    }

    fun update(
        caseDefinitionName: String,
        @Valid requests: List<CaseTagUpdateRequestDto>
    ): List<CaseTag> {
        denyManagementOperation()

        val existingCaseTags = caseTagRepository
            .findByIdCaseDefinitionNameOrderByOrder(caseDefinitionName)
        check(existingCaseTags.size == requests.size) {
            throw IllegalStateException(
                "Failed to update case tags. Reason: the number of "
                    + "case tags in the update request does not match the number of existing case tags."
            )
        }

        val updatedCaseTags = requests.mapIndexed { index, request ->
            val existingCaseTag = existingCaseTags.find { it.id.key == request.key }
                ?: throw CaseTagNotFoundException(request.key, caseDefinitionName)
            existingCaseTag.copy(
                title = request.title,
                color = request.color,
                order = index
            )
        }

        return caseTagRepository.saveAll(updatedCaseTags)
    }

    fun delete(caseDefinitionName: String, caseTagKey: String) {
        denyManagementOperation()

        val caseTag =
            caseTagRepository.findDistinctByIdCaseDefinitionNameAndIdKey(
                caseDefinitionName, caseTagKey
            ) ?: throw CaseTagNotFoundException(caseTagKey, caseDefinitionName)

        if (caseTagRepository.isCaseTagInUse(caseTagKey, caseDefinitionName)) {
            throw CaseTagInUseException(caseTagKey, caseDefinitionName)
        }

        caseTagRepository.delete(caseTag)
        reorder(caseDefinitionName)
    }

    private fun reorder(caseDefinitionName: String) {
        val caseTags = caseTagRepository.findByIdCaseDefinitionNameOrderByOrder(
            caseDefinitionName
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
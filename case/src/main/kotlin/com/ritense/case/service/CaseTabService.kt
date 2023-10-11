/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.authorization.Action.Companion.deny
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.TAB_ORDER
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionName
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionNameAndTabKey
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case.web.rest.dto.CaseTabUpdateDto
import com.ritense.case.web.rest.dto.CaseTabUpdateOrderDto
import com.ritense.document.service.DocumentDefinitionService
import kotlin.jvm.optionals.getOrNull
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseTabService(
    private val caseTabRepository: CaseTabRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    private val authorizationService: AuthorizationService
) {

    @Transactional(readOnly = true)
    fun getCaseTabs(caseDefinitionName: String): List<CaseTab> {
        return caseTabRepository.findAll(byCaseDefinitionName(caseDefinitionName), Sort.by(TAB_ORDER))
    }

    fun createCaseTab(caseDefinitionName: String, caseTabDto: CaseTabDto): CaseTabDto {
        denyAuthorization()

        documentDefinitionService.findLatestByName(caseDefinitionName).getOrNull()
            ?: throw NoSuchElementException("Case definition with name $caseDefinitionName does not exist!")

        val caseTab = CaseTab(
            CaseTabId(caseDefinitionName, caseTabDto.key),
            caseTabDto.name,
            getCaseTabs(caseDefinitionName).size, // Add it to the end
            caseTabDto.type,
            caseTabDto.contentKey
        )

        val savedTab = caseTabRepository.save(caseTab)
        return CaseTabDto.of(savedTab)
    }

    fun updateCaseTab(caseDefinitionName: String, tabKey: String, caseTab: CaseTabUpdateDto) {
        denyAuthorization()

        val existingTab = caseTabRepository.findOne(byCaseDefinitionNameAndTabKey(caseDefinitionName, tabKey)).get()

        caseTabRepository.save(
            existingTab.copy(
                name = caseTab.name,
                type = caseTab.type,
                contentKey = caseTab.contentKey
            )
        )
    }

    fun updateCaseTabs(caseDefinitionName: String, caseTabDtos: List<CaseTabUpdateOrderDto>): List<CaseTab> {
        denyAuthorization()

        val existingTabs = caseTabRepository.findAll(byCaseDefinitionName(caseDefinitionName))
        if (existingTabs.size != caseTabDtos.size) {
            throw IllegalStateException("Failed to update tabs. Reason: the number of tabs in the update request doesn't match the number of existing tabs.")
        }

        val updatedTabs = caseTabDtos.mapIndexed { index, caseTabDto ->
            val existingTab = existingTabs.find { it.id.key == caseTabDto.key }
                ?: throw IllegalStateException("Failed to update tabs. Reason: tab with key '${caseTabDto.key}' doesn't exist.")
            existingTab.copy(
                name = caseTabDto.name,
                tabOrder = index,
                type = caseTabDto.type,
                contentKey = caseTabDto.contentKey
            )
        }

        return caseTabRepository.saveAll(updatedTabs)
    }

    fun deleteCaseTab(caseDefinitionName: String, tabKey: String) {
        denyAuthorization()

        caseTabRepository.findOne(byCaseDefinitionNameAndTabKey(caseDefinitionName, tabKey))
            .ifPresent {
                caseTabRepository.delete(it)
                reorderTabs(caseDefinitionName)
            }
    }

    private fun reorderTabs(caseDefinitionName: String) {
        val caseTabs = caseTabRepository.findAll(byCaseDefinitionName(caseDefinitionName), Sort.by(TAB_ORDER))
            .mapIndexed { index, caseTab -> caseTab.copy(tabOrder = index)  }
        caseTabRepository.saveAll(caseTabs)
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseTab::class.java,
                deny()
            )
        )
    }
}

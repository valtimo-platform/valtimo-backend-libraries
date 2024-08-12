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

package com.ritense.case.repository

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseTabDocumentDefinitionMapperIT @Autowired constructor(
    private val caseTabService: CaseTabService,
) : BaseIntegrationTest() {

    @BeforeEach
    fun setup() {
        runWithoutAuthorization {
            val tabs = caseTabService.getCaseTabs("definition-test")
            tabs.forEach { caseTabService.deleteCaseTab("definition-test", it.id.key) }

            caseTabService.createCaseTab("definition-test", CaseTabDto(key = "test-tab-1", type = CaseTabType.WIDGETS, contentKey = "-"))
            caseTabService.createCaseTab("definition-test", CaseTabDto(key = "test-tab-2", type = CaseTabType.WIDGETS, contentKey = "-"))
            caseTabService.createCaseTab("other-case-type", CaseTabDto(key = "test-tab-3", type = CaseTabType.WIDGETS, contentKey = "-"))
            caseTabService.createCaseTab("other-case-type", CaseTabDto(key = "test-tab-4", type = CaseTabType.WIDGETS, contentKey = "-"))
        }
    }

    @Test
    @WithMockUser(authorities = ["ROLE_ONLY_TEST_DEFINITION_TABS"])
    fun `should only get tabs for related definition`() {
        val definitionTestTabs = caseTabService.getCaseTabs("definition-test")
        val otherCaseTabs = caseTabService.getCaseTabs("other-case-type")

        assertEquals(2, definitionTestTabs.size)
        assertEquals(0, otherCaseTabs.size)

        assertEquals("test-tab-1", definitionTestTabs[0].id.key)
        assertEquals("test-tab-2", definitionTestTabs[1].id.key)
    }
}
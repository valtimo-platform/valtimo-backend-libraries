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

package com.ritense.case.deployment

import com.ritense.BaseIntegrationTest
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.annotation.RunWithoutAuthorizationAspect
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseDefinitionDraftCreateRequest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@Transactional
class CaseTabDeploymentServiceIT @Autowired constructor(
    private val caseDefinitionService: CaseDefinitionService,
    private val caseTabService: CaseTabService
) : BaseIntegrationTest() {

    @Autowired
    private lateinit var runWithoutAuthorizationAspect: RunWithoutAuthorizationAspect

    @Test
    fun `should create tabs on new case definition`() {
        runWithoutAuthorization {
            val caseDefinitionKey = "tab-test-case-definition"
            val caseDefinitionVersion = "1.0.0"

            caseDefinitionService.createCaseDefinitionDraft(
                CaseDefinitionDraftCreateRequest(
                    caseDefinitionKey = caseDefinitionKey,
                    caseDefinitionVersion = caseDefinitionVersion,
                    name = "Tab Test Case Definition",
                    description = "This is a tab test case definition.",
                    basedOnCaseDefinitionVersion = null
                )
            )

            val tabs = caseTabService.getCaseTabs(CaseDefinitionId(caseDefinitionKey, caseDefinitionVersion))
            assertEquals(5, tabs.size)
            assertThat(tabs.map { it.id.key }).containsExactlyInAnyOrder(
                "summary", "progress", "audit", "documents", "notes"
            )
        }
    }

    @Test
    fun `should not create tabs on new case definition during import`() {
        runWithoutAuthorization {
            // see if any of the imported case definitions have tabs that are not defined in the import
            val caseDefinitionKey = "house"
            val caseDefinitionVersion = "1.1.0"

            val tabs = caseTabService.getCaseTabs(CaseDefinitionId(caseDefinitionKey, caseDefinitionVersion))
            assertEquals(0, tabs.size)
        }
    }

    @Test
    fun `should not create tabs on draft of existing definition`() {
        runWithoutAuthorization {
            val caseDefinitionKey = "tab-test-case-definition"
            val oldCaseDefinitionVersion = "1.0.0"
            val newCaseDefinitionVersion = "1.1.0"
            val oldCaseDefinitionId = CaseDefinitionId(caseDefinitionKey, oldCaseDefinitionVersion)
            val newCaseDefinitionId = CaseDefinitionId(caseDefinitionKey, newCaseDefinitionVersion)

            caseDefinitionService.createCaseDefinitionDraft(
                CaseDefinitionDraftCreateRequest(
                    caseDefinitionKey = caseDefinitionKey,
                    caseDefinitionVersion = oldCaseDefinitionVersion,
                    name = "Tab Test Case Definition",
                    description = "This is a tab test case definition.",
                    basedOnCaseDefinitionVersion = null
                )
            )
            // make sure there are no tabs on the old draft
            caseTabService.deleteCaseTabs(oldCaseDefinitionId)
            caseDefinitionService.finalizeCaseDefinition(oldCaseDefinitionId)

            //create a new draft of the same case definition
            caseDefinitionService.createCaseDefinitionDraft(
                CaseDefinitionDraftCreateRequest(
                    caseDefinitionKey = caseDefinitionKey,
                    caseDefinitionVersion = newCaseDefinitionVersion,
                    name = "Tab Test Case Definition",
                    description = "This is a tab test case definition.",
                    basedOnCaseDefinitionVersion = oldCaseDefinitionVersion
                )
            )

            val tabs = caseTabService.getCaseTabs(newCaseDefinitionId)
            assertEquals(0, tabs.size)
        }
    }
}
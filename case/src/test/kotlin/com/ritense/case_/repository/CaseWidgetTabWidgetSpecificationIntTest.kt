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

package com.ritense.case_.repository

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.case_.web.rest.dto.TestCaseWidgetTabWidgetDto
import com.ritense.case_.widget.TestCaseWidgetProperties
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

@Transactional
class CaseWidgetTabWidgetSpecificationIntTest @Autowired constructor(
    private val caseTabService: CaseTabService,
    private val caseWidgetTabService: CaseWidgetTabService
) : BaseIntegrationTest() {

    val caseDefinitionName = "widgets"
    val tabKey = "some-tab"

    @BeforeEach
    fun setup() {
        runWithoutAuthorization {
            caseTabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))

            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey,
                    widgets = listOf(
                        TestCaseWidgetTabWidgetDto("test", "Widget 1", 0, false, TestCaseWidgetProperties("test123")),
                        TestCaseWidgetTabWidgetDto("other-widget", "Widget 2", 1, true, TestCaseWidgetProperties("test123")),
                    )
                )
            )
        }
    }

    @Test
    @WithMockUser(authorities = ["ROLE_ALL_WIDGETS"])
    fun `should get tab with all widgets`() {
        val tab = caseWidgetTabService.getWidgetTab(caseDefinitionName, tabKey)

        assertEquals(2, tab?.widgets?.size)
        assertEquals("test", tab?.widgets?.get(0)?.key)
        assertEquals("other-widget", tab?.widgets?.get(1)?.key)
    }

    @Test
    @WithMockUser(authorities = ["ROLE_ONLY_TEST_WIDGETS"])
    fun `should get tab with only permitted widgets`() {
        val tab = caseWidgetTabService.getWidgetTab(caseDefinitionName, tabKey)

        assertEquals(1, tab?.widgets?.size)
        assertEquals("test", tab?.widgets?.get(0)?.key)
    }
}
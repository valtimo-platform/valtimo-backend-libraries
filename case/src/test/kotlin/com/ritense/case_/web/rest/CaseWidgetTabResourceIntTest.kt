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

package com.ritense.case_.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.case_.web.rest.dto.TestCaseWidgetTabWidgetDto
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.DEVELOPER
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
class CaseWidgetTabResourceIntTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val tabService: CaseTabService,
    private val widgetTabService: CaseWidgetTabService
) : BaseIntegrationTest() {

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [USER])
    fun `should not find case widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "fake-tab"
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}", caseDefinitionName, tabKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [DEVELOPER])
    fun `should deny access to widget tab when not authorized`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"
        runWithoutAuthorization {
            tabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
        }
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}", caseDefinitionName, tabKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().is5xxServerError)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [USER])
    fun `should find case widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"
        runWithoutAuthorization {
            tabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
        }
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}", caseDefinitionName, tabKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.caseDefinitionName").value(caseDefinitionName))
            .andExpect(jsonPath("$.key").value(tabKey))
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [DEVELOPER])
    fun `should deny access to case widget data when not authorized`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"
        val widgetKey = "my-widget"
        runWithoutAuthorization {
            tabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
            widgetTabService.updateWidgetTab(CaseWidgetTabDto(
                caseDefinitionName, tabKey, widgets = listOf(
                    TestCaseWidgetTabWidgetDto(
                        widgetKey, "My widget", 0, true)
                )
            )
            )
        }
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}/widget/{widgetKey}", caseDefinitionName, tabKey, widgetKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().is5xxServerError)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [USER])
    fun `should get case widget data`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"
        val widgetKey = "my-widget"
        runWithoutAuthorization {
            tabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
            widgetTabService.updateWidgetTab(CaseWidgetTabDto(
                caseDefinitionName, tabKey, widgets = listOf(
                    TestCaseWidgetTabWidgetDto(
                        widgetKey, "My widget", 0, true)
                    )
                )
            )
        }
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}/widget/{widgetKey}", caseDefinitionName, tabKey, widgetKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.test").value("test123"))
    }
}

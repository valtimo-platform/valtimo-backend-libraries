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

package com.ritense.case_.widget.custom

import com.ritense.BaseIntegrationTest
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.json.MapperSingleton
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
class CustomWidgetIntTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val tabService: CaseTabService,
    private val widgetTabService: CaseWidgetTabService,
) : BaseIntegrationTest() {

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should find case widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"
        val widgetKey = "my-widget"
        val documentId = runWithoutAuthorization {
            val document = documentService.createDocument(
                NewDocumentRequest(
                    caseDefinitionName,
                    caseDefinitionName,
                    "1.2.3",
                    MapperSingleton.get().createObjectNode()
                )
            ).resultingDocument().get()
            createCaseWidgetTab(document.definitionId().caseDefinitionId(), tabKey, widgetKey)
            document.id
        }
        mockMvc.perform(
            get("/api/v1/document/{documentId}/widget-tab/{tabKey}", documentId, tabKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.widgets[0].type").value("custom"))
            .andExpect(jsonPath("$.widgets[0].title").value("Custom test"))
            .andExpect(jsonPath("$.widgets[0].properties").exists())
            .andExpect(jsonPath("$.widgets[0].properties.componentKey").exists())
            .andExpect(jsonPath("$.widgets[0].properties.componentKey").value("test-component-key"))
    }

    private fun createCaseWidgetTab(
        caseDefinitionId: CaseDefinitionId,
        tabKey: String,
        widgetKey: String
    ): CaseWidgetTabDto {
        tabService.createCaseTab(
            caseDefinitionId,
            CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-")
        )
        return widgetTabService.updateWidgetTab(
            CaseWidgetTabDto(
                caseDefinitionKey = caseDefinitionId.key,
                caseDefinitionVersionTag = caseDefinitionId.versionTag.version,
                key = tabKey,
                widgets = listOf(
                    CustomCaseWidgetDto(
                        key = widgetKey,
                        title = "Custom test",
                        width = 2,
                        highContrast = false,
                        actions = null,
                        properties = CustomWidgetProperties("test-component-key")
                    )
                )
            )
        )
    }
}

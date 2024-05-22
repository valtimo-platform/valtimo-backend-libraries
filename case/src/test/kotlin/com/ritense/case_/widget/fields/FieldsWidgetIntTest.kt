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

package com.ritense.case_.widget.fields

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.case_.widget.displayproperties.BooleanFieldDisplayProperties
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
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
class FieldsWidgetIntTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val tabService: CaseTabService,
    private val widgetTabService: CaseWidgetTabService,
    private val documentService: JsonSchemaDocumentService
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
        runWithoutAuthorization {
            createCaseWidgetTab(caseDefinitionName, tabKey, widgetKey)
        }
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}", caseDefinitionName, tabKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.widgets[0].type").value("fields"))
            .andExpect(jsonPath("$.widgets[0].properties").exists())
            .andExpect(jsonPath("$.widgets[0].properties.columns").exists())
            .andExpect(jsonPath("$.widgets[0].properties.columns[0][0].key").value("someKey"))
            .andExpect(jsonPath("$.widgets[0].properties.columns[0][0].title").value("Some key"))
            .andExpect(jsonPath("$.widgets[0].properties.columns[0][0].value").value("test:/myKey"))
            .andExpect(jsonPath("$.widgets[0].properties.columns[0][0].displayProperties").doesNotExist())
            .andExpect(jsonPath("$.widgets[0].properties.columns[1][0].key").value("someOtherKey"))
            .andExpect(jsonPath("$.widgets[0].properties.columns[1][0].title").value("Some other key"))
            .andExpect(jsonPath("$.widgets[0].properties.columns[1][0].value").value("test:/myOtherKey"))
            .andExpect(jsonPath("$.widgets[0].properties.columns[1][0].displayProperties").exists())
            .andExpect(jsonPath("$.widgets[0].properties.columns[1][0].displayProperties.type").value("boolean"))
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should get case widget data`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"
        val widgetKey = "my-widget"
        val documentId = runWithoutAuthorization {
            createCaseWidgetTab(caseDefinitionName, tabKey, widgetKey)
            documentService.createDocument(NewDocumentRequest(caseDefinitionName, MapperSingleton.get().createObjectNode())).resultingDocument().get().id()
        }
        mockMvc.perform(
            get("/api/v1/document/{documentId}/widget-tab/{tabKey}/widget/{widgetKey}", documentId, tabKey, widgetKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.someKey").value("test:/myKey"))
            .andExpect(jsonPath("$.someOtherKey").value("test:/myOtherKey"))
    }

    private fun createCaseWidgetTab(
        caseDefinitionName: String,
        tabKey: String,
        widgetKey: String
    ): CaseWidgetTabDto {
        tabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
        return widgetTabService.updateWidgetTab(
            CaseWidgetTabDto(
                caseDefinitionName = caseDefinitionName,
                key = tabKey,
                widgets = listOf(
                    FieldsCaseWidgetDto(widgetKey, "My widget", 1, true, FieldsWidgetProperties(
                        columns = listOf(
                            listOf(
                                FieldsWidgetProperties.Field(
                                    "someKey", "Some key", "test:/myKey"
                                )
                            ),
                            listOf(
                                FieldsWidgetProperties.Field(
                                    "someOtherKey",
                                    "Some other key",
                                    "test:/myOtherKey",
                                    displayProperties = BooleanFieldDisplayProperties()
                                )
                            )
                        )
                    )
                )
            )
        )
        )
    }
}

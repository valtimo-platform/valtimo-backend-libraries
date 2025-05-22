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

package com.ritense.case_.widget.collection

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.TestResolverFactory
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.case_.widget.displayproperties.BooleanFieldDisplayProperties
import com.ritense.case_.widget.fields.FieldsWidgetProperties
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
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
import java.util.function.Function

@Transactional
class CollectionWidgetIntTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val tabService: CaseTabService,
    private val widgetTabService: CaseWidgetTabService,
    private val documentService: JsonSchemaDocumentService,
    private val testValueResolverFactory: TestResolverFactory
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
            createCaseWidgetTab(caseDefinitionName, tabKey, widgetKey)
            documentService.createDocument(NewDocumentRequest(caseDefinitionName, MapperSingleton.get().createObjectNode())).resultingDocument().get().id()
        }
        mockMvc.perform(
            get("/api/v1/document/{documentId}/widget-tab/{tabKey}", documentId, tabKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.widgets[0].type").value("collection"))
            .andExpect(jsonPath("$.widgets[0].properties").exists())
            .andExpect(jsonPath("$.widgets[0].properties.collection").value("test:myCollection"))
            .andExpect(jsonPath("$.widgets[0].properties.defaultPageSize").value(5))
            .andExpect(jsonPath("$.widgets[0].properties.fields").exists())
            .andExpect(jsonPath("$.widgets[0].properties.fields[0].key").value("someKey"))
            .andExpect(jsonPath("$.widgets[0].properties.fields[0].title").value("Some key"))
            .andExpect(jsonPath("$.widgets[0].properties.fields[0].value").value("$.someKey"))
            .andExpect(jsonPath("$.widgets[0].properties.fields[0].displayProperties").doesNotExist())
            .andExpect(jsonPath("$.widgets[0].properties.fields[1].key").value("someOtherKey"))
            .andExpect(jsonPath("$.widgets[0].properties.fields[1].title").value("Some other key"))
            .andExpect(jsonPath("$.widgets[0].properties.fields[1].value").value("/someOtherKey"))
            .andExpect(jsonPath("$.widgets[0].properties.fields[1].displayProperties").exists())
            .andExpect(jsonPath("$.widgets[0].properties.fields[1].displayProperties.type").value("boolean"))
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

        whenever(testValueResolverFactory.createResolver(documentId.toString())).thenReturn(Function { collectionPlaceholder ->
            assertThat(collectionPlaceholder).isEqualTo("myCollection")

            listOf(mapOf(
                "someTitleKey" to "z",
                "someKey" to "x",
                "someOtherKey" to "y"
            ))
        })
        mockMvc.perform(
            get("/api/v1/document/{documentId}/widget-tab/{tabKey}/widget/{widgetKey}", documentId, tabKey, widgetKey)
                .param("size", "1337")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.numberOfElements").value(1))
            .andExpect(jsonPath("$.size").value(1337))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.content[0].title").value("z"))
            .andExpect(jsonPath("$.content[0].fields.someKey").value("x"))
            .andExpect(jsonPath("$.content[0].fields.someOtherKey").value("y"))
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
                    CollectionCaseWidgetDto(
                        widgetKey, "My widget", 1, false, null, CollectionWidgetProperties(
                            collection = "test:myCollection",
                            defaultPageSize = 5,
                            title = CollectionWidgetProperties.TitleField(
                                "$.someTitleKey"
                            ),
                            fields = listOf(
                                CollectionWidgetProperties.Field(
                                    "someKey",
                                    "Some key",
                                    "$.someKey",
                                    CollectionWidgetProperties.FieldWidth.FULL
                                ),
                                CollectionWidgetProperties.Field(
                                    "someOtherKey",
                                    "Some other key",
                                    "/someOtherKey",
                                    CollectionWidgetProperties.FieldWidth.HALF,
                                    displayProperties = BooleanFieldDisplayProperties()
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}

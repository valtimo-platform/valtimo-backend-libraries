/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.case.web.rest

import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.document.service.DocumentDefinitionService
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
class CaseDefinitionResourceIntTest : BaseIntegrationTest() {
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository

    @Autowired
    lateinit var caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository

    val LIST_COLUMN_PATH: String = "/api/v1/case/{caseDefinitionName}/list-column"

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun `should get case settings with default values`() {
        documentDefinitionService.deploy(
            "" +
                    "{\n" +
                    "    \"\$id\": \"resource-test-default.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
        )
        val caseDefinitionName = "resource-test-default"
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/v1/case/{caseDefinitionName}/settings", caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(false))
    }

    @Test
    fun `should update case settings`() {
        documentDefinitionService.deploy(
            "" +
                    "{\n" +
                    "    \"\$id\": \"resource-test-update.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
        )


        val caseDefinitionName = "resource-test-update"
        mockMvc.perform(
            MockMvcRequestBuilders.patch(
                "/api/v1/case/{caseDefinitionName}/settings", caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE).content("{\"canHaveAssignee\": true}")
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))
        val settingsInDatabase = caseDefinitionSettingsRepository.getById(caseDefinitionName)
        assertEquals(true, settingsInDatabase.canHaveAssignee)
        assertEquals(caseDefinitionName, settingsInDatabase.name)
    }

    @Test
    fun `should not update case settings property when it has not been submitted`() {
        val caseDefinitionName = "resource-test-empty"
        documentDefinitionService.deploy(
            "{\n" +
                    "    \"\$id\": \"$caseDefinitionName.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
        )
        val settings = CaseDefinitionSettings(caseDefinitionName, true)
        caseDefinitionSettingsRepository.save(settings)
        mockMvc.perform(
            MockMvcRequestBuilders.patch(
                "/api/v1/case/{caseDefinitionName}/settings", caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE).content("{}")
        )
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))
        val settingsInDatabase = caseDefinitionSettingsRepository.getById(caseDefinitionName)
        assertEquals(true, settingsInDatabase.canHaveAssignee)
        assertEquals(caseDefinitionName, settingsInDatabase.name)
    }

    @Test
    fun `should return not found when getting settings for case that does not exist`() {
        val caseDefinitionName = "some-case-that-does-not-exist"
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/v1/case/{caseDefinitionName}/settings", caseDefinitionName
            )
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `should return not found when updating settings for case that does not exist`() {
        val caseDefinitionName = "some-case-that-does-not-exist"
        mockMvc.perform(
            MockMvcRequestBuilders.patch(
                "/api/v1/case/{caseDefinitionName}/settings", caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE).content("{\"canHaveAssignee\": true}")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `should create list column`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        documentDefinitionService.deploy(
            "{\n" +
                    "    \"\$id\": \"listColumnDocumentDefinition.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                    "    \"title\": \"listColumnDocumentDefinition\",\n" +
                    "    \"type\": \"object\",\n" +
                    "    \"properties\": {\n" +
                    "        \"firstName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"first name\"\n" +
                    "        },\n" +
                    "        \"lastName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"last name\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post(
                LIST_COLUMN_PATH, caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                "{\n" + "  \"title\": \"First name\",\n" + "  \"key\": \"first-name\",\n" + "  \"path\": \"doc:firstName\" ,\n" + "  \"displayType\": {\n" + "    \"type\": \"enum\",\n" + "    \"displayTypeParameters\": {\n" + "        \"enum\": {\"key1\":\"Value 1\"},\n" + "        \"date-format\": \"\"\n" + "        }\n" + "    },\n" + "    \"sortable\": true ,\n" + "    \"defaultSort\": \"ASC\"\n" + "}"
            )
        ).andExpect(status().isOk)
    }

    @Test
    fun `should return bad request on create`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        documentDefinitionService.deploy(
            "{\n" +
                    "  \"\$id\": \"listColumnDocumentDefinition.schema\",\n" +
                    "  \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                    "  \"title\": \"listColumnDocumentDefinition\",\n" +
                    "  \"type\": \"object\",\n" +
                    "  \"properties\": {\n" +
                    "    \"firstName\": {\n" +
                    "      \"type\": \"string\",\n" +
                    "      \"description\": \"first name\"\n" +
                    "    },\n" +
                    "    \"lastName\": {\n" +
                    "      \"type\": \"string\",\n" +
                    "      \"description\": \"last name\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"
        )
        createListColumn(
            caseDefinitionName,
            """
                    {
                      "title": "First name",
                      "key": "first-name",
                      "path": "doc:firstName",
                      "displayType": {
                        "type": "enum",
                        "displayTypeParameters": {
                          "date-format": ""
                        }
                      },
                      "sortable": true,
                      "defaultSort": "ASC"
                    }
                """.trimIndent(), status().isBadRequest
        )
    }

    @Test
    fun `should return bad request on get with invalid document definition`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                LIST_COLUMN_PATH, caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `should return columns for document definition`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        `should create list column`()
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                LIST_COLUMN_PATH, caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect {
                status().isOk
                content().json(
                    """
                [
                  {
                    "title": "First name",
                    "key": "first-name",
                    "path": "doc:firstName",
                    "displayType": {
                      "type": "enum",
                      "displayTypeParameters": {
                        "enum": {
                          "key1": "Value 1"
                        }
                      }
                    },
                    "sortable": true,
                    "defaultSort": "ASC"
                  }
                ]
            """.trimIndent()
                )
            }
    }

    @Test
    fun `should update columns for case definition`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        documentDefinitionService.deploy(
            "{\n" +
                    "    \"\$id\": \"listColumnDocumentDefinition.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                    "    \"title\": \"listColumnDocumentDefinition\",\n" +
                    "    \"type\": \"object\",\n" +
                    "    \"properties\": {\n" +
                    "        \"firstName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"first name\"\n" +
                    "        },\n" +
                    "        \"lastName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"last name\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"
        )
        createListColumn(
            caseDefinitionName,
            """
                          {
                            "title": "First name",
                            "key": "first-name",
                            "path": "doc:firstName",
                            "displayType": {
                              "type": "enum",
                              "displayTypeParameters": {
                                "enum": {
                                  "key1": "Value 1"
                                }
                              }
                            },
                            "sortable": true,
                            "defaultSort": "ASC"
                          }
                """.trimIndent(), status().isOk
        )
        createListColumn(
            caseDefinitionName,
            """
                          {
                            "title": "Last name",
                            "key": "last-name",
                            "path": "doc:lastName",
                            "displayType": {
                              "type": "enum",
                              "displayTypeParameters": {
                                "enum": {
                                  "key1": "Value 1"
                                }
                              }
                            },
                            "sortable": true
                          }
                """.trimIndent(), status().isOk
        )
        mockMvc.perform(
            MockMvcRequestBuilders.put(LIST_COLUMN_PATH, caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                    """
                        [
                          {
                            "title": "Last name",
                            "key": "last-name",
                            "path": "doc:lastName",
                            "displayType": {
                              "type": "enum",
                              "displayTypeParameters": {
                                "enum": {
                                  "key1": "Value 1"
                                }
                              }
                            },
                            "sortable": true,
                            "defaultSort": "DESC"
                          },
                          {
                            "title": "First name",
                            "key": "first-name",
                            "path": "doc:firstName",
                            "displayType": {
                              "type": "enum",
                              "displayTypeParameters": {
                                "enum": {
                                  "key1": "Value 1"
                                }
                              }
                            },
                            "sortable": true
                          }
                        ]
                    """.trimIndent()
                )
        ).andExpect(status().isOk)
        val columns = caseDefinitionListColumnRepository
            .findByIdCaseDefinitionNameOrderByOrderAscSortableAsc(caseDefinitionName)
        assertEquals("last-name", columns[0].id.key)
        assertEquals(ColumnDefaultSort.DESC, columns[0].defaultSort)
        assertEquals("first-name", columns[1].id.key)
        assertNull(columns[1].defaultSort)
    }

    @Test
    fun `should delete column for case definition`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        documentDefinitionService.deploy(
            "{\n" +
                    "    \"\$id\": \"listColumnDocumentDefinition.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                    "    \"title\": \"listColumnDocumentDefinition\",\n" +
                    "    \"type\": \"object\",\n" +
                    "    \"properties\": {\n" +
                    "        \"firstName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"first name\"\n" +
                    "        },\n" +
                    "        \"lastName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"last name\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"
        )
        val columnKey = "first-name"
        createListColumn(
            caseDefinitionName,
            """
                          {
                            "title": "First name",
                            "key": "$columnKey",
                            "path": "doc:firstName",
                            "displayType": {
                              "type": "enum",
                              "displayTypeParameters": {
                                "enum": {
                                  "key1": "Value 1"
                                }
                              }
                            },
                            "sortable": true,
                            "defaultSort": "ASC"
                          }
                """.trimIndent(), status().isOk
        )
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$LIST_COLUMN_PATH/{columnKey}", caseDefinitionName, columnKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `should respond with no content for non existing column`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        documentDefinitionService.deploy(
            "{\n" +
                    "    \"\$id\": \"listColumnDocumentDefinition.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                    "    \"title\": \"listColumnDocumentDefinition\",\n" +
                    "    \"type\": \"object\",\n" +
                    "    \"properties\": {\n" +
                    "        \"firstName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"first name\"\n" +
                    "        },\n" +
                    "        \"lastName\": {\n" +
                    "            \"type\": \"string\",\n" +
                    "            \"description\": \"last name\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"
        )
        val columnKey = "first-name"
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$LIST_COLUMN_PATH/{columnKey}", caseDefinitionName, columnKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `should respond bad request for non existing case definition`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        val columnKey = "first-name"
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$LIST_COLUMN_PATH/{columnKey}", caseDefinitionName, columnKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isBadRequest)
    }

    private fun createListColumn(caseDefinitionName: String, json: String, expectedStatus: ResultMatcher) {
        mockMvc.perform(
            MockMvcRequestBuilders.post(LIST_COLUMN_PATH, caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json)
        ).andDo { result -> print(result.response.contentAsString) }.andExpect(expectedStatus)
    }
}
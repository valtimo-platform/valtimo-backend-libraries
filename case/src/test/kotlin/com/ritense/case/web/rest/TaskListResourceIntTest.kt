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

package com.ritense.case.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Transactional
class TaskListResourceIntTest : BaseIntegrationTest() {
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

    @Deprecated("Since 11.0.0")
    @Test
    fun `should create list column`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        runWithoutAuthorization {
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
        }
        mockMvc.perform(
            MockMvcRequestBuilders.post(
                LIST_COLUMN_PATH, caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                "{\n" + "  \"title\": \"First name\",\n" + "  \"key\": \"first-name\",\n" + "  \"path\": \"test:firstName\" ,\n" + "  \"displayType\": {\n" + "    \"type\": \"enum\",\n" + "    \"displayTypeParameters\": {\n" + "        \"enum\": {\"key1\":\"Value 1\"},\n" + "        \"date-format\": \"\"\n" + "        }\n" + "    },\n" + "    \"sortable\": true ,\n" + "    \"defaultSort\": \"ASC\"\n" + "}"
            )
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should create list column as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        runWithoutAuthorization {
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
        }
        mockMvc.perform(
            MockMvcRequestBuilders.put(
                "/api/management/v1/case/{caseDefinitionName}/task-list-column/{columnKey}", caseDefinitionName, "first-name"
            ).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                "{\n" + "  \"title\": \"First name\",\n" + "  \"key\": \"first-name\",\n" + "  \"path\": \"test:firstName\" ,\n" + "  \"displayType\": {\n" + "    \"type\": \"enum\",\n" + "    \"displayTypeParameters\": {\n" + "        \"enum\": {\"key1\":\"Value 1\"},\n" + "        \"date-format\": \"\"\n" + "        }\n" + "    },\n" + "    \"sortable\": true ,\n" + "    \"defaultSort\": \"ASC\"\n" + "}"
            )
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should return bad request on create as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        runWithoutAuthorization {
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
        }
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
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should return bad request on get with invalid document definition as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/management/v1/case/{caseDefinitionName}/list-column", caseDefinitionName
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [USER])
    fun `should return columns for document definition as a user`() {
        `should return columns for document definition`("/api/v1/case/{caseDefinitionName}/list-column")
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should return columns for document definition as an admin`() {
        `should return columns for document definition`("/api/management/v1/case/{caseDefinitionName}/list-column")
    }

    fun `should return columns for document definition`(urlTemplate: String) {
        val caseDefinitionName = "listColumnDocumentDefinition"
        `should create list column`()
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/management/v1/case/{caseDefinitionName}/list-column", caseDefinitionName
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
                    "path": "test:firstName",
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
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should update columns for case definition as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        runWithoutAuthorization {
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
        }
        createListColumn(
            caseDefinitionName,
            """
                          {
                            "title": "First name",
                            "key": "first-name",
                            "path": "test:firstName",
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
                            "path": "test:lastName",
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
            MockMvcRequestBuilders.put("/api/management/v1/case/{caseDefinitionName}/list-column", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                    """
                        [
                          {
                            "title": "Last name",
                            "key": "last-name",
                            "path": "test:lastName",
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
                            "path": "test:firstName",
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
            .findByIdCaseDefinitionNameOrderByOrderAsc(caseDefinitionName)
        assertEquals("last-name", columns[0].id.key)
        assertEquals(ColumnDefaultSort.DESC, columns[0].defaultSort)
        assertEquals("first-name", columns[1].id.key)
        assertNull(columns[1].defaultSort)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should delete column for case definition as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        runWithoutAuthorization {
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
        }
        val columnKey = "first-name"
        createListColumn(
            caseDefinitionName,
            """
                          {
                            "title": "First name",
                            "key": "$columnKey",
                            "path": "test:firstName",
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
            MockMvcRequestBuilders.delete("/api/management/v1/case/{caseDefinitionName}/list-column/{columnKey}", caseDefinitionName, columnKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should respond with no content for non existing column as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        runWithoutAuthorization {
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
        }
        val columnKey = "first-name"
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/management/v1/case/{caseDefinitionName}/list-column/{columnKey}", caseDefinitionName, columnKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should respond bad request for non existing case definition as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        val columnKey = "first-name"
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/management/v1/case/{caseDefinitionName}/list-column/{columnKey}", caseDefinitionName, columnKey)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should insert list column with correct order as an admin`() {
        val caseDefinitionName = "listColumnDocumentDefinition"
        runWithoutAuthorization {
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
        }
        createListColumn(
            caseDefinitionName,
            """
                          {
                            "title": "First name",
                            "key": "first-name",
                            "path": "test:firstName",
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
                            "path": "test:firstName",
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
            MockMvcRequestBuilders.get(
                "/api/management/v1/case/{caseDefinitionName}/list-column", caseDefinitionName
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
                    "path": "test:firstName",
                    "displayType": {
                      "type": "enum",
                      "displayTypeParameters": {
                        "enum": {
                          "key1": "Value 1"
                        }
                      }
                    },
                    "order": 0,
                    "sortable": true,
                    "defaultSort": "ASC"
                  },
                  {
                    "title": "Last name",
                    "key": "last-name",
                    "path": "test:lastName",
                    "displayType": {
                      "type": "enum",
                      "displayTypeParameters": {
                        "enum": {
                          "key1": "Value 1"
                        }
                      }
                    },
                    "order": 1,
                    "sortable": true,
                    "defaultSort": null
                  }
                ]
            """.trimIndent()
                )
            }
    }

    private fun createListColumn(caseDefinitionName: String, json: String, expectedStatus: ResultMatcher) {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/management/v1/case/{caseDefinitionName}/task-list-column/{columnKey}", caseDefinitionName, "test")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json)
        ).andDo { result -> print(result.response.contentAsString) }.andExpect(expectedStatus)
    }
}

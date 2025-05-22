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

package com.ritense.documentenapi.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.documentenapi.BaseIntegrationTest
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.IDENTIFICATIE
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.TITEL
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import org.hamcrest.Matchers.containsInRelativeOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertNull

@Transactional
internal class DocumentenApiManagementResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var documentenApiService: DocumentenApiService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService

    @Autowired
    lateinit var documentenApiColumnRepository: DocumentenApiColumnRepository

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should get a list of all Documenten API column keys`() {
        runWithoutAuthorization {
            documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                "profile",
                DocumentDefinitionProcessRequest("call-activity-to-upload-document", "DOCUMENT_UPLOAD")
            )
        }

        mockMvc.perform(
            get("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column-key", "profile")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value("auteur"))
            .andExpect(jsonPath("$[0].sortable").value(true))
            .andExpect(jsonPath("$[0].filterable").value(true))
            .andExpect(jsonPath("$[1].key").value("beschrijving"))
            .andExpect(jsonPath("$[1].sortable").value(false))
            .andExpect(jsonPath("$[1].filterable").value(true))
    }

    @Test
    fun `should get a list of ordered Documenten API columns`() {
        runWithoutAuthorization {
            documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                "profile",
                DocumentDefinitionProcessRequest("call-activity-to-upload-document", "DOCUMENT_UPLOAD")
            )
        }

        documentenApiColumnRepository.deleteAllByIdCaseDefinitionName("profile")

        runWithoutAuthorization {
            documentenApiService.createOrUpdateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("profile", IDENTIFICATIE), 0)
            )
            documentenApiService.createOrUpdateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("profile", TITEL), 1)
            )
        }

        mockMvc.perform(
            get("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column", "profile")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value("identificatie"))
            .andExpect(jsonPath("$[0].sortable").value(false))
            .andExpect(jsonPath("$[0].filterable").value(false))
            .andExpect(jsonPath("$[1].key").value("titel"))
            .andExpect(jsonPath("$[1].sortable").value(true))
            .andExpect(jsonPath("$[1].filterable").value(true))
    }

    @Test
    fun `should reorder list of Documenten API columns`() {
        runWithoutAuthorization {
            documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                "profile",
                DocumentDefinitionProcessRequest("call-activity-to-upload-document", "DOCUMENT_UPLOAD")
            )
        }
        documentenApiColumnRepository.deleteAllByIdCaseDefinitionName("profile")

        runWithoutAuthorization {
            documentenApiService.createOrUpdateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("profile", IDENTIFICATIE))
            )
            documentenApiService.createOrUpdateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("profile", TITEL))
            )
        }

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column", "profile")
                .contentType(APPLICATION_JSON_VALUE)
                .content(
                    """
                    [
                        {
                            "key": "titel",
                            "defaultSort": "asc"
                        },
                        {
                            "key": "identificatie"
                        }
                    ]
                """.trimMargin()
                )
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value("titel"))
            .andExpect(jsonPath("$[0].sortable").value(true))
            .andExpect(jsonPath("$[0].filterable").value(true))
            .andExpect(jsonPath("$[1].key").value("identificatie"))
            .andExpect(jsonPath("$[1].sortable").value(false))
            .andExpect(jsonPath("$[1].filterable").value(false))
    }

    @Test
    fun `should update Documenten API column`() {
        runWithoutAuthorization {
            documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                "profile",
                DocumentDefinitionProcessRequest("call-activity-to-upload-document", "DOCUMENT_UPLOAD")
            )
        }
        runWithoutAuthorization {
            documentenApiService.createOrUpdateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("profile", TITEL))
            )
        }

        mockMvc.perform(
            put(
                "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column/{columnKey}",
                "profile",
                "titel"
            )
                .contentType(APPLICATION_JSON_VALUE)
                .content(
                    """
                    {
                        "defaultSort": "asc"
                    }
                """.trimMargin()
                )
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("titel"))
            .andExpect(jsonPath("$.defaultSort").value("ASC"))
    }

    @Test
    fun `should delete Documenten API column`() {
        runWithoutAuthorization {
            documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                "profile",
                DocumentDefinitionProcessRequest("call-activity-to-upload-document", "DOCUMENT_UPLOAD")
            )
            documentenApiService.createOrUpdateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("profile", TITEL))
            )
        }

        mockMvc.perform(
            delete(
                "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column/{columnKey}",
                "profile",
                "titel"
            )
        )
            .andDo(print())
            .andExpect(status().isOk)

        assertNull(documentenApiColumnRepository.findByIdCaseDefinitionNameAndIdKey("profile", TITEL))
    }

    @Test
    fun `should get API version`() {
        runWithoutAuthorization {
            documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                "profile",
                DocumentDefinitionProcessRequest("call-activity-to-upload-document", "DOCUMENT_UPLOAD")
            )
        }

        mockMvc.perform(
            get(
                "/api/management/v1/case-definition/{caseDefinitionName}/documenten-api/version",
                "profile"
            )
        )
            .andDo(print())
            .andExpect(jsonPath("$.selectedVersion").value("1.5.0-test-1.0.0"))
            .andExpect(jsonPath("$.detectedVersions.size()").value(1))
            .andExpect(jsonPath("$.detectedVersions[0]").value("1.5.0-test-1.0.0"))
    }

    @Test
    fun `should get all API versions`() {
        mockMvc.perform(get("/api/management/v1/documenten-api/versions"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.versions", containsInRelativeOrder(
                        "1.5.0-test-1.0.0",
                        "1.5.0",
                        "1.4.3",
                        "1.4.2",
                        "1.4.1",
                        "1.4.0",
                        "1.3.0",
                        "1.2.3",
                        "1.2.2",
                        "1.2.1",
                        "1.2.0",
                        "1.1.0",
                        "1.0.1",
                        "1.0.0"
                    )
                )
            )
    }
}

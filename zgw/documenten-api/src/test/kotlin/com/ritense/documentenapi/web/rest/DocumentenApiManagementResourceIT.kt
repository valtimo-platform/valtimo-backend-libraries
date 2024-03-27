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
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

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

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should get a list of ordered Documenten API columns`() {
        runWithoutAuthorization {
            documentenApiService.updateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("myCaseDefinition", IDENTIFICATIE))
            )
            documentenApiService.updateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("myCaseDefinition", TITEL))
            )
        }

        mockMvc.perform(
            get("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column", "myCaseDefinition")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(jsonPath("$[0].key").value("identificatie"))
            .andExpect(jsonPath("$[0].enabled").value(true))
            .andExpect(jsonPath("$[1].key").value("titel"))
            .andExpect(jsonPath("$[1].enabled").value(true))
    }

    @Test
    fun `should reorder list of Documenten API columns`() {
        runWithoutAuthorization {
            documentenApiService.updateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("myCaseDefinition", IDENTIFICATIE))
            )
            documentenApiService.updateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("myCaseDefinition", TITEL))
            )
        }

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column", "myCaseDefinition")
                .contentType(APPLICATION_JSON_VALUE)
                .content("""
                    [
                        {
                            "key": "titel",
                            "enabled": true
                        },
                        {
                            "key": "identificatie",
                            "enabled": true
                        },
                        {
                            "key": "creatiedatum",
                            "enabled": false
                        },
                        {
                            "key": "auteur",
                            "enabled": false
                        },
                        {
                            "key": "bestandsomvang",
                            "enabled": false
                        },
                        {
                            "key": "informatieobjecttype",
                            "enabled": false
                        },
                        {
                            "key": "bronorganisatie",
                            "enabled": false
                        },
                        {
                            "key": "vertrouwelijkheidaanduiding",
                            "enabled": false
                        },
                        {
                            "key": "status",
                            "enabled": false
                        },
                        {
                            "key": "formaat",
                            "enabled": false
                        },
                        {
                            "key": "taal",
                            "enabled": false
                        },
                        {
                            "key": "versie",
                            "enabled": false
                        },
                        {
                            "key": "bestandsnaam",
                            "enabled": false
                        },
                        {
                            "key": "beschrijving",
                            "enabled": false
                        },
                        {
                            "key": "locked",
                            "enabled": false
                        }
                    ]
                """.trimMargin())
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(jsonPath("$[0].key").value("titel"))
            .andExpect(jsonPath("$[0].enabled").value(true))
            .andExpect(jsonPath("$[1].key").value("identificatie"))
            .andExpect(jsonPath("$[1].enabled").value(true))
    }

    @Test
    fun `should update Documenten API column`() {
        runWithoutAuthorization {
            documentenApiService.updateColumn(
                DocumentenApiColumn(DocumentenApiColumnId("myCaseDefinition", IDENTIFICATIE))
            )
        }

        mockMvc.perform(
            put(
                "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column/{columnKey}",
                "myCaseDefinition",
                "identificatie"
            )
                .contentType(APPLICATION_JSON_VALUE)
                .content("""
                    {
                        "enabled": false
                    }
                """.trimMargin())
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(jsonPath("$.key").value("identificatie"))
            .andExpect(jsonPath("$.enabled").value(false))
    }

    @Test
    fun `should get API version`() {
        runWithoutAuthorization {
            documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                "profile",
                DocumentDefinitionProcessRequest("call-activity-to-upload-document", "DOCUMENT_UPLOAD")
            )
        }

        mockMvc.perform(get("/api/management/v1/case-definition/{caseDefinitionName}/documenten-api/version", "profile"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(jsonPath("$.selectedVersion").value("1.2.0"))
            .andExpect(jsonPath("$.detectedVersions.size()").value(1))
            .andExpect(jsonPath("$.detectedVersions[0]").value("1.2.0"))
    }
}

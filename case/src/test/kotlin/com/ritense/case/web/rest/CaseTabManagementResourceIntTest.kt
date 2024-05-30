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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.case.domain.CaseTabType
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationHelper
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case.web.rest.dto.CaseTabUpdateDto
import com.ritense.case.web.rest.dto.CaseTabUpdateOrderDto
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.Constants
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import kotlin.jvm.optionals.getOrNull

class CaseTabManagementResourceIntTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val caseTabRepository: CaseTabRepository,
    private val documentDefinitionService: DocumentDefinitionService
) : BaseIntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should create tab`() {
        val caseDefinitionName = "some-case-type"

        runWithoutAuthorization {
            documentDefinitionService.deploy("""
                {
                    "${'$'}id": "$caseDefinitionName.schema",
                    "${'$'}schema": "http://json-schema.org/draft-07/schema#"
                }
            """.trimIndent())
        }

        val dto = CaseTabDto(
            key = "some-key",
            name = "Some name",
            type = CaseTabType.STANDARD,
            contentKey = "some-content-key"
        )

        assertThat(
            caseTabRepository.findOne(
                CaseTabSpecificationHelper.byCaseDefinitionNameAndTabKey(
                    caseDefinitionName,
                    dto.key
                )
            ).getOrNull()
        ).isNull()

        mockMvc.perform(
            post("/api/management/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.key").value(dto.key))
            .andExpect(jsonPath("$.name").value(dto.name))
            .andExpect(jsonPath("$.type").value(dto.type.value))
            .andExpect(jsonPath("$.contentKey").value(dto.contentKey))

        val createdTab = caseTabRepository.findOne(
            CaseTabSpecificationHelper.byCaseDefinitionNameAndTabKey(caseDefinitionName, dto.key)
        ).getOrNull()

        assertThat(createdTab).isNotNull()
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should fail creating tab when tab with key already exists`() {
        val caseDefinitionName = "some-case-type"

        runWithoutAuthorization {
            documentDefinitionService.deploy("""
                {
                    "${'$'}id": "$caseDefinitionName.schema",
                    "${'$'}schema": "http://json-schema.org/draft-07/schema#"
                }
            """.trimIndent())
        }

        val dto = CaseTabDto(
            key = "some-key",
            name = "Some name",
            type = CaseTabType.STANDARD,
            contentKey = "some-content-key"
        )

        mockMvc.perform(
            post("/api/management/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/management/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isConflict)

        val createdTab = caseTabRepository.findOne(
            CaseTabSpecificationHelper.byCaseDefinitionNameAndTabKey(caseDefinitionName, dto.key)
        ).getOrNull()

        assertThat(createdTab).isNotNull()
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should update tab order`() {
        val caseDefinitionName = "test-case-type"

        caseTabRepository.saveAll(
            listOf(
                CaseTab(
                    id = CaseTabId(caseDefinitionName, "tab-1"),
                    name = "Tab 1",
                    type = CaseTabType.STANDARD,
                    tabOrder = 0,
                    contentKey = "some-content-key"
                ),
                CaseTab(
                    id = CaseTabId(caseDefinitionName, "tab-2"),
                    name = "Tab 2",
                    type = CaseTabType.STANDARD,
                    tabOrder = 1,
                    contentKey = "some-content-key"
                )
            )
        )

        val updateDto = listOf(
            CaseTabUpdateOrderDto(
                key = "tab-2",
                name = "Tab 2",
                type = CaseTabType.STANDARD,
                contentKey = "some-content-key"
            ), CaseTabUpdateOrderDto(
                key = "tab-1",
                name = "Tab 1",
                type = CaseTabType.STANDARD,
                contentKey = "some-content-key"
            )
        )

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value("tab-2"))
            .andExpect(jsonPath("$[1].key").value("tab-1"))
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should update tab`() {
        val caseDefinitionName = "some-case-type"

        val key = "some-key"
        val caseTab = CaseTab(
            id = CaseTabId(caseDefinitionName, key),
            name = "Some tab name",
            type = CaseTabType.STANDARD,
            tabOrder = Integer.MAX_VALUE,
            contentKey = "some-content-key"
        )

        caseTabRepository.save(caseTab)

        val updateDto = CaseTabUpdateDto(
            name = "Some updated tab name",
            type = CaseTabType.CUSTOM,
            contentKey = "some-updated-content-key"
        )

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionName}/tab/{tabKey}", caseDefinitionName, key)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isNoContent)

        val updatedTab = caseTabRepository.findOne(
            CaseTabSpecificationHelper.byCaseDefinitionNameAndTabKey(caseDefinitionName, key)
        ).getOrNull()

        assertThat(updatedTab).isNotNull()
        check(updatedTab != null)
        assertThat(updatedTab.name).isEqualTo(updatedTab.name)
        assertThat(updatedTab.type).isEqualTo(updatedTab.type)
        assertThat(updatedTab.tabOrder).isEqualTo(caseTab.tabOrder)
        assertThat(updatedTab.contentKey).isEqualTo(updatedTab.contentKey)
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should delete tab`() {
        val caseDefinitionName = "some-case-type"

        val key = "some-key"
        val caseTab = CaseTab(
            id = CaseTabId(caseDefinitionName, key),
            name = "Some tab name",
            type = CaseTabType.STANDARD,
            tabOrder = Integer.MAX_VALUE,
            contentKey = "some-content-key"
        )

        caseTabRepository.save(caseTab)

        mockMvc.perform(
            delete("/api/management/v1/case-definition/{caseDefinitionName}/tab/{tabKey}", caseDefinitionName, key)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isNoContent)

        assertThat(
            caseTabRepository.findOne(
                CaseTabSpecificationHelper.byCaseDefinitionNameAndTabKey(
                    caseDefinitionName,
                    key
                )
            ).getOrNull()
        ).isNull()
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should get tabs`() {
        val caseDefinitionName = "my-case-type"

        val key = "some-key"
        val caseTab = CaseTab(
            id = CaseTabId(caseDefinitionName, key),
            name = "Some tab name",
            type = CaseTabType.STANDARD,
            tabOrder = Integer.MAX_VALUE,
            contentKey = "some-content-key"
        )

        caseTabRepository.save(caseTab)

        mockMvc.perform(
            get("/api/management/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value(caseTab.id.key))
            .andExpect(jsonPath("$[0].name").value(caseTab.name))
            .andExpect(jsonPath("$[0].type").value(caseTab.type.value))
            .andExpect(jsonPath("$[0].contentKey").value(caseTab.contentKey))
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `should get tab by id`() {
        whenever(userManagementService.findById("system")).thenReturn(
            ValtimoUserBuilder().id(Constants.SYSTEM_ACCOUNT).lastName(
                Constants.SYSTEM_ACCOUNT
            ).build()
        )
        val caseDefinitionName = "my-case-type"

        val key = "some-key"
        val caseTab = CaseTab(
            id = CaseTabId(caseDefinitionName, key),
            name = "Some tab name",
            type = CaseTabType.STANDARD,
            tabOrder = Integer.MAX_VALUE,
            contentKey = "some-content-key",
            createdBy = "system"
        )

        caseTabRepository.save(caseTab)

        mockMvc.perform(
            get("/api/management/v1/case-definition/{caseDefinitionName}/tab/{tabKey}", caseDefinitionName, key)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value(caseTab.id.key))
            .andExpect(jsonPath("$.name").value(caseTab.name))
            .andExpect(jsonPath("$.type").value(caseTab.type.value))
            .andExpect(jsonPath("$.contentKey").value(caseTab.contentKey))
            .andExpect(jsonPath("$.createdBy").value("system"))
            .andExpect(jsonPath("$.createdOn").exists())
    }
}

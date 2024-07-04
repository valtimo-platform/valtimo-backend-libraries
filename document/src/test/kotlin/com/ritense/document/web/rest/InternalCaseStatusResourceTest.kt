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

package com.ritense.document.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.document.BaseTest
import com.ritense.document.domain.InternalCaseStatus
import com.ritense.document.domain.InternalCaseStatusColor
import com.ritense.document.domain.InternalCaseStatusId
import com.ritense.document.service.InternalCaseStatusService
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateOrderRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateRequestDto
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.json.MapperSingleton
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class InternalCaseStatusResourceTest : BaseTest() {

    private val caseDefinitionName = "test"
    private val internalCaseStatuses = IntRange(0, 4).map { i ->
        createInternalCaseStatus("key-$i", i)
    }

    private lateinit var mockMvc: MockMvc
    private lateinit var internalCaseStatusService: InternalCaseStatusService


    @BeforeEach
    fun setUp() {
        internalCaseStatusService = mock()
        val internalCaseStatusResource = InternalCaseStatusResource(internalCaseStatusService)
        mockMvc = MockMvcBuilders.standaloneSetup(internalCaseStatusResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build()

        whenever(internalCaseStatusService.getInternalCaseStatuses(caseDefinitionName)).thenReturn(internalCaseStatuses)
    }

    @Test
    fun `should get internalCaseStatuses`() {
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/internal-status", caseDefinitionName)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize<Any>(internalCaseStatuses.size)))
            .let {
                val status = internalCaseStatuses[0]
                it.andExpect(jsonPath("$[0].key").value(status.id.key))
                    .andExpect(jsonPath("$[0].caseDefinitionName").value(status.id.caseDefinitionName))
                    .andExpect(jsonPath("$[0].title").value(status.title))
                    .andExpect(jsonPath("$[0].order").value(status.order))
                    .andExpect(jsonPath("$[0].color").value(status.color.name))
            }
    }

    @Test
    fun `should get internalCaseStatuses for management`() {
        mockMvc.perform(
            get("/api/management/v1/case-definition/{caseDefinitionName}/internal-status", caseDefinitionName)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize<Any>(internalCaseStatuses.size)))
            .let {
                val status = internalCaseStatuses[1]
                it.andExpect(jsonPath("$[1].key").value(status.id.key))
                    .andExpect(jsonPath("$[1].caseDefinitionName").value(status.id.caseDefinitionName))
                    .andExpect(jsonPath("$[1].title").value(status.title))
                    .andExpect(jsonPath("$[1].order").value(status.order))
                    .andExpect(jsonPath("$[1].color").value(status.color.name))
            }
    }

    @Test
    fun `should create internalCaseStatus`() {
        val json = """
                    {
                        "key": "test",
                        "title": "Test",
                        "visibleInCaseListByDefault": false,
                        "color": "RED"
                    }
                """.trimIndent()
        val requestDto = jacksonObjectMapper().readValue<InternalCaseStatusCreateRequestDto>(json)
        whenever(internalCaseStatusService.create(eq(caseDefinitionName), eq(requestDto))).thenReturn(requestDto.toInternalCaseStatus())
        mockMvc.perform(
            post("/api/management/v1/case-definition/{caseDefinitionName}/internal-status", caseDefinitionName)
                .content(json)
                .characterEncoding(Charsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.key").value("test"))
            .andExpect(jsonPath("$.caseDefinitionName").value(caseDefinitionName))
            .andExpect(jsonPath("$.title").value("Test"))
            .andExpect(jsonPath("$.order").value(0))
            .andExpect(jsonPath("$.color").value("RED"))
    }

    @Test
    fun `should reorder a list of internalCaseStatuses`() {
        // PUT /management/v1/case-definition/{caseDefinitionName}/internal-status
        val requests = internalCaseStatuses.map { it.toUpdateOrderRequestDto() }.shuffled()
        whenever(internalCaseStatusService.update(eq(caseDefinitionName), eq(requests))).thenReturn(
            requests.mapIndexed { index, dto -> dto.toInternalCaseStatus(index) }
        )
        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionName}/internal-status", caseDefinitionName)
                .content(jacksonObjectMapper().writeValueAsString(requests))
                .characterEncoding(Charsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .let {
                requests.forEachIndexed { i, dto ->
                    it.andExpect(jsonPath("$[$i].key").value(dto.key))
                        .andExpect(jsonPath("$[$i].caseDefinitionName").value(caseDefinitionName))
                        .andExpect(jsonPath("$[$i].title").value(dto.title))
                        .andExpect(jsonPath("$[$i].order").value(i))
                        .andExpect(jsonPath("$[$i].color").value(dto.color.name))
                }

            }

    }

    @Test
    fun `should update a internalCaseStatus`() {
        val updateDto = InternalCaseStatusUpdateRequestDto("test", "Test", true, InternalCaseStatusColor.GRAY)

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionName}/internal-status/{key}", caseDefinitionName, updateDto.key)
                .content(jacksonObjectMapper().writeValueAsString(updateDto))
                .characterEncoding(Charsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent())

        verify(internalCaseStatusService).update(eq(caseDefinitionName), eq(updateDto.key), eq(updateDto))
    }

    @Test
    fun `should delete a internalCaseStatus`() {
        mockMvc.perform(
            delete("/api/management/v1/case-definition/{caseDefinitionName}/internal-status/{key}", caseDefinitionName, "test")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())

        verify(internalCaseStatusService).delete(eq(caseDefinitionName), eq("test"))
    }

    private fun createInternalCaseStatus(statusKey: String, order: Int): InternalCaseStatus {
        return InternalCaseStatus(
            InternalCaseStatusId(caseDefinitionName, statusKey),
            statusKey.replaceFirstChar { it.uppercase() },
            true,
            order,
            InternalCaseStatusColor.entries[order]
        )
    }

    private fun InternalCaseStatus.toUpdateOrderRequestDto(): InternalCaseStatusUpdateOrderRequestDto {
        return InternalCaseStatusUpdateOrderRequestDto(
            this.id.key, this.title, this.visibleInCaseListByDefault, this.color
        )
    }

    private fun InternalCaseStatusCreateRequestDto.toInternalCaseStatus() : InternalCaseStatus {
        return InternalCaseStatus(
            InternalCaseStatusId(caseDefinitionName, this.key),
            this.title,
            this.visibleInCaseListByDefault,
            0,
            this.color
        )
    }

    private fun InternalCaseStatusUpdateOrderRequestDto.toInternalCaseStatus(order: Int): InternalCaseStatus {
        return InternalCaseStatus(
            InternalCaseStatusId(caseDefinitionName, this.key),
            this.title,
            this.visibleInCaseListByDefault,
            order,
            this.color
        )
    }
}
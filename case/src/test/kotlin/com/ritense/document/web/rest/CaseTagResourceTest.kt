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
import com.ritense.BaseTest
import com.ritense.document.domain.CaseTag
import com.ritense.document.domain.CaseTagColor
import com.ritense.document.domain.CaseTagId
import com.ritense.document.service.CaseTagService
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import com.ritense.valtimo.contract.case_.CaseDefinitionId
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

class CaseTagResourceTest : BaseTest() {
    private val caseDefinitionId = CaseDefinitionId.of("test", "1.0.0")

    private val caseTags = IntRange(0, 4).map { i ->
        createCaseTags("key-$i", i)
    }

    private lateinit var mockMvc: MockMvc
    private lateinit var caseTagService: CaseTagService

    @BeforeEach
    fun setUp() {
        caseTagService = mock()
        val caseTagResource = CaseTagResource(caseTagService)
        mockMvc = MockMvcBuilders.standaloneSetup(caseTagResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build()

        whenever(caseTagService.getCaseTags(caseDefinitionId)).thenReturn(caseTags)
    }

    @Test
    fun `should get caseTags`() {
        mockMvc.perform(
            get(
                "/api/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/case-tag",
                caseDefinitionId.key,
                caseDefinitionId.versionTag.version
            )
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize<Any>(caseTags.size)))
            .let {
                val status = caseTags[0]
                it.andExpect(jsonPath("$[0].key").value(status.id.key))
                    .andExpect(jsonPath("$[0].caseDefinitionKey").value(status.id.caseDefinitionId.key))
                    .andExpect(jsonPath("$[0].caseDefinitionVersionTag").value(status.id.caseDefinitionId.versionTag.version))
                    .andExpect(jsonPath("$[0].title").value(status.title))
                    .andExpect(jsonPath("$[0].order").value(status.order))
                    .andExpect(jsonPath("$[0].color").value(status.color.name))
            }
    }

    @Test
    fun `should get caseTags for management`() {
        mockMvc.perform(
            get(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/case-tag",
                caseDefinitionId.key,
                caseDefinitionId.versionTag.version
            )
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize<Any>(caseTags.size)))
            .let {
                val status = caseTags[1]
                it.andExpect(jsonPath("$[1].key").value(status.id.key))
                    .andExpect(jsonPath("$[1].caseDefinitionKey").value(status.id.caseDefinitionId.key))
                    .andExpect(jsonPath("$[1].caseDefinitionVersionTag").value(status.id.caseDefinitionId.versionTag.version))
                    .andExpect(jsonPath("$[1].title").value(status.title))
                    .andExpect(jsonPath("$[1].order").value(status.order))
                    .andExpect(jsonPath("$[1].color").value(status.color.name))
            }
    }

    @Test
    fun `should create caseTag`() {
        val json = """
                    {
                        "key": "test",
                        "title": "Test",
                        "color": "RED"
                    }
                """.trimIndent()
        val requestDto = jacksonObjectMapper().readValue<CaseTagCreateRequestDto>(json)
        whenever(caseTagService.create(eq(caseDefinitionId), eq(requestDto))).thenReturn(requestDto.toCaseTag())
        mockMvc.perform(
            post(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/case-tag",
                caseDefinitionId.key,
                caseDefinitionId.versionTag.version
            )
                .content(json)
                .characterEncoding(Charsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.key").value("test"))
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinitionId.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinitionId.versionTag.version))
            .andExpect(jsonPath("$.title").value("Test"))
            .andExpect(jsonPath("$.order").value(0))
            .andExpect(jsonPath("$.color").value("RED"))
    }

    @Test
    fun `should reorder a list of caseTags`() {
        val requests = caseTags.map { it.toUpdateRequestDto() }.shuffled()
        whenever(caseTagService.update(eq(caseDefinitionId), eq(requests))).thenReturn(
            requests.mapIndexed { index, dto -> dto.toCaseTag(index) }
        )
        mockMvc.perform(
            put(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/case-tag",
                caseDefinitionId.key,
                caseDefinitionId.versionTag.version
            )
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
                        .andExpect(jsonPath("$[$i].caseDefinitionKey").value(caseDefinitionId.key))
                        .andExpect(jsonPath("$[$i].caseDefinitionVersionTag").value(caseDefinitionId.versionTag.version))
                        .andExpect(jsonPath("$[$i].title").value(dto.title))
                        .andExpect(jsonPath("$[$i].order").value(i))
                        .andExpect(jsonPath("$[$i].color").value(dto.color.name))
                }

            }
    }

    @Test
    fun `should update a caseTag`() {
        val updateDto = CaseTagUpdateRequestDto("test", "Test", CaseTagColor.GRAY)

        mockMvc.perform(
            put(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/case-tag/{key}",
                caseDefinitionId.key,
                caseDefinitionId.versionTag.version,
                updateDto.key
            )
                .content(jacksonObjectMapper().writeValueAsString(updateDto))
                .characterEncoding(Charsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent())

        verify(caseTagService).update(eq(caseDefinitionId), eq(updateDto.key), eq(updateDto))
    }

    @Test
    fun `should delete a caseTag`() {
        mockMvc.perform(
            delete(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/case-tag/{key}",
                caseDefinitionId.key,
                caseDefinitionId.versionTag,
                "test"
            )
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())

        verify(caseTagService).delete(eq(caseDefinitionId), eq("test"))
    }

    private fun createCaseTags(statusKey: String, order: Int): CaseTag {
        return CaseTag(
            CaseTagId(caseDefinitionId, statusKey),
            statusKey.replaceFirstChar { it.uppercase() },
            CaseTagColor.entries[order],
            order
        )
    }

    private fun CaseTag.toUpdateRequestDto(): CaseTagUpdateRequestDto {
        return CaseTagUpdateRequestDto(
            this.id.key,
            this.title,
            this.color
        )
    }

    private fun CaseTagCreateRequestDto.toCaseTag(): CaseTag {
        return CaseTag(
            CaseTagId(caseDefinitionId, this.key),
            this.title,
            this.color,
            0
        )
    }

    private fun CaseTagUpdateRequestDto.toCaseTag(order: Int): CaseTag {
        return CaseTag(
            CaseTagId(caseDefinitionId, this.key),
            this.title,
            this.color,
            order
        )
    }
}
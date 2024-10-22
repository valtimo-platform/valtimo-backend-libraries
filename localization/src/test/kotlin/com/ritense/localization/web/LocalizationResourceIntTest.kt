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

package com.ritense.localization.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.localization.BaseIntegrationTest
import com.ritense.localization.domain.Localization
import com.ritense.localization.repository.LocalizationRepository
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
class LocalizationResourceIntTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val localizationRepository: LocalizationRepository,
    private val objectMapper: ObjectMapper
): BaseIntegrationTest() {
    lateinit var mockMvc: MockMvc
    lateinit var enLocalization: Localization
    lateinit var nlLocalization: Localization

    @BeforeEach
    fun beforeEach() {
        clean()

        enLocalization = Localization(EN_LOCALE_STRING, objectMapper.readValue<ObjectNode>(EN_JSON))
        nlLocalization = Localization(NL_LOCALE_STRING, objectMapper.readValue<ObjectNode>(NL_JSON))

        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @AfterEach
    fun afterEach() {
        clean()
    }

    private fun clean() {
        localizationRepository.deleteAll()
    }

    @Test
    fun `should get localizations`() {

        localizationRepository.saveAll(listOf(enLocalization, nlLocalization))

        mockMvc.perform(
            get("/api/v1/localization")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].languageKey").value(EN_LOCALE_STRING))
            .andExpect(jsonPath("$[1].languageKey").value(NL_LOCALE_STRING))
            .andExpect(jsonPath("$[0].content.title").value("title"))
            .andExpect(jsonPath("$[1].content.title").value("titel"))
    }

    @Test
    fun `should get specific localization`() {

        localizationRepository.save(enLocalization)

        mockMvc.perform(
            get("/api/v1/localization/${EN_LOCALE_STRING}")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("title"))
    }

    @Test
    fun `should return empty response when retrieving non-existent localization`() {
        mockMvc.perform(
            get("/api/v1/localization/${EN_LOCALE_STRING}")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `should save specific localization`() {
        mockMvc.perform(
            put("/api/management/v1/localization/${EN_LOCALE_STRING}").content(EN_JSON).contentType(APPLICATION_JSON_UTF8_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("title"))

        val enLocalization = localizationRepository.findById(EN_LOCALE_STRING).orElseThrow()

        Assertions.assertThat(enLocalization.content.get("title").textValue()).isEqualTo("title")
    }

    @Test
    fun `should save localizations`() {
        val localizationsString = """
            [
                {
                    "languageKey": "${EN_LOCALE_STRING}",
                    "content": ${EN_JSON}
                },
                {
                    "languageKey": "${NL_LOCALE_STRING}",
                    "content": ${NL_JSON}
                }
            ]
        """.trimIndent()

        mockMvc.perform(
            put("/api/management/v1/localization").content(localizationsString).contentType(APPLICATION_JSON_UTF8_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].languageKey").value(EN_LOCALE_STRING))
            .andExpect(jsonPath("$[1].languageKey").value(NL_LOCALE_STRING))
            .andExpect(jsonPath("$[0].content.title").value("title"))
            .andExpect(jsonPath("$[1].content.title").value("titel"))

        val enLocalization = localizationRepository.findById(EN_LOCALE_STRING).orElseThrow()
        val nlLocalization = localizationRepository.findById(NL_LOCALE_STRING).orElseThrow()

        Assertions.assertThat(enLocalization.content.get("title").textValue()).isEqualTo("title")
        Assertions.assertThat(nlLocalization.content.get("title").textValue()).isEqualTo("titel")
    }

    companion object {
        val EN_JSON = """
            {
                "title": "title"
            }
        """.trimIndent()
        val NL_JSON = """
            {
                "title": "titel"
            }
        """.trimIndent()
        const val EN_LOCALE_STRING = "en"
        const val NL_LOCALE_STRING = "nl"
    }
}
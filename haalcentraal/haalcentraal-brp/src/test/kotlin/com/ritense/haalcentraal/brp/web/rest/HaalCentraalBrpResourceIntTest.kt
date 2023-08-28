/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.haalcentraal.brp.web.rest

import com.ritense.haalcentraal.brp.BaseIntegrationTest
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@EnableAutoConfiguration
class HaalCentraalBrpResourceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    override fun setUp() {
        super.setUp()
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun `should get person by bsn`() {
        val request = """
            {
                "bsn":"555555021"
            }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/haalcentraal/personen")
            .content(request)
            .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[0].burgerservicenummer").value("555555021"))
            .andExpect(jsonPath("$.[0].voornamen").value("Pieter Jan"))
            .andExpect(jsonPath("$.[0].voorletters").value("P.J."))
            .andExpect(jsonPath("$.[0].geslachtsnaam").value("Vries"))
            .andExpect(jsonPath("$.[0].geboorteDatum").value("1989-05-03"))
    }

    @Test
    fun `should get persons by name and date`() {
        val request = """
            {
                "geslachtsnaam":"Vries",
                "geboortedatum":"1989-05-03"
            }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/haalcentraal/personen")
            .content(request)
            .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[0].burgerservicenummer").value("555555021"))
            .andExpect(jsonPath("$.[0].voornamen").value("Pieter Jan"))
            .andExpect(jsonPath("$.[0].voorletters").value("P.J."))
            .andExpect(jsonPath("$.[0].geslachtsnaam").value("Vries"))
            .andExpect(jsonPath("$.[0].geboorteDatum").value("1989-05-03"))
    }

    @Test
    fun `should fail on invalid request`() {
        val request = """
            {
                "geslachtsnaam":"Vries"
            }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/haalcentraal/personen")
            .content(request)
            .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().is5xxServerError)
            .andExpect(jsonPath("$.detail").value("When not searching with a bsn the name and birthdate must both be filled in"))
    }
}

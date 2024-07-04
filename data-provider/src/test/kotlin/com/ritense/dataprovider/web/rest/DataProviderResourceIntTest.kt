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

package com.ritense.dataprovider.web.rest

import com.ritense.dataprovider.BaseIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
internal class DataProviderResourceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @Test
    fun `should get all provider names`() {
        mockMvc.perform(get("/api/v1/data/dropdown-list/provider"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0]").value("dropdownDatabaseDataProvider"))
            .andExpect(jsonPath("$[1]").value("dropdownJsonFileDataProvider"))
    }

    @Test
    fun `should get data by provider name`() {
        mockMvc.perform(get("/api/v1/data/dropdown-list?provider=dropdownJsonFileDataProvider&key=user-dropdown-list"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.u00007").value("Mary Johnson"))
            .andExpect(jsonPath("$.u00008").value("Patricia Williams"))
            .andExpect(jsonPath("$.u00009").value("Jennifer Smith"))
    }

    @Test
    fun `should get error when for non existing provider`() {
        mockMvc.perform(get("/api/v1/data/dropdown-list?provider=nonExistingDataProvider&key=user-dropdown-list"))
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

}

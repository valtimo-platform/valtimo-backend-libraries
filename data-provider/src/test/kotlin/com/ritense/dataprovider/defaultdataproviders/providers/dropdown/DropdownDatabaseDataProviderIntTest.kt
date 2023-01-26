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

package com.ritense.dataprovider.defaultdataproviders.providers.dropdown

import com.ritense.dataprovider.BaseIntegrationTest
import com.ritense.dataprovider.defaultdataproviders.domain.DropdownList
import com.ritense.dataprovider.defaultdataproviders.repository.DropdownListRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
internal class DropdownDatabaseDataProviderIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var dropdownListRepository: DropdownListRepository

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()

        dropdownListRepository.save(
            DropdownList(
                key = "my-test-user-list",
                values = mutableMapOf(
                    "u00001" to "James Vance",
                    "u00002" to "John Doe",
                    "u00003" to "Asha Miller"
                )
            )
        )
    }

    @Test
    fun `should get all dropdown list entries from database`() {
        mockMvc.perform(get("/api/v1/data/dropdown-list?key=my-test-user-list"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.u00001").value("James Vance"))
            .andExpect(jsonPath("$.u00002").value("John Doe"))
            .andExpect(jsonPath("$.u00003").value("Asha Miller"))
    }

    @Test
    fun `should append dropdown list`() {
        mockMvc.perform(
            post("/api/v1/data/dropdown-list?key=my-test-user-list&append=true")
                .content("""{"u00004": "John Doe"}""")
                .contentType(APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        val dropdownValues = dropdownListRepository.getReferenceById("my-test-user-list").values
        assertEquals(4, dropdownValues.size)
        assertEquals("James Vance", dropdownValues["u00001"])
        assertEquals("John Doe", dropdownValues["u00002"])
        assertEquals("Asha Miller", dropdownValues["u00003"])
        assertEquals("John Doe", dropdownValues["u00004"])
    }

    @Test
    fun `should override dropdown list`() {
        mockMvc.perform(
            post("/api/v1/data/dropdown-list?key=my-test-user-list")
                .content("""{"u00004": "John Doe"}""")
                .contentType(APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        val dropdownValues = dropdownListRepository.getReferenceById("my-test-user-list").values
        assertEquals(1, dropdownValues.size)
        assertEquals("John Doe", dropdownValues["u00004"])
    }

    @Test
    fun `should create dropdown list when none exist`() {
        mockMvc.perform(
            post("/api/v1/data/dropdown-list?key=new-user-list")
                .content("""{"u00004": "John Doe"}""")
                .contentType(APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        val dropdownValues = dropdownListRepository.getReferenceById("new-user-list").values
        assertEquals(1, dropdownValues.size)
        assertEquals("John Doe", dropdownValues["u00004"])
    }

    @Test
    fun `should delete dropdown list`() {
        mockMvc.perform(delete("/api/v1/data/dropdown-list?key=my-test-user-list"))
            .andDo(print())
            .andExpect(status().isNoContent)

        assertTrue(dropdownListRepository.findById("my-test-user-list").isEmpty)
    }

    @Test
    fun `should delete dropdown list entry`() {
        mockMvc.perform(delete("/api/v1/data/dropdown-list?key=my-test-user-list&value=u00002"))
            .andDo(print())
            .andExpect(status().isNoContent)

        val dropdownValues = dropdownListRepository.getReferenceById("my-test-user-list").values
        assertEquals(2, dropdownValues.size)
        assertEquals("James Vance", dropdownValues["u00001"])
        assertEquals("Asha Miller", dropdownValues["u00003"])
    }

}

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

package com.ritense.contactmoment.web.rest.impl

import com.ritense.contactmoment.BaseContactMomentIntegrationTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

internal class ContactMomentResourceIT : BaseContactMomentIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    override fun setUp() {
        super.setUp()
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    @WithMockUser
    fun `post contactmoment saves contact moment`() {
        mockUser(lastName = "Miller")
        val postBody = """
            {
                "kanaal": "MAIL",
                "tekst": "content-2"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/contactmoment")
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.registratiedatum").isNotEmpty)
            .andExpect(jsonPath("$.kanaal").value("MAIL"))
            .andExpect(jsonPath("$.tekst").value("content-2"))
            .andExpect(jsonPath("$.medewerkerIdentificatie.achternaam").value("Miller"))
            .andReturn()
    }

    @Test
    @WithMockUser
    fun `get kanalen responds with all kanalen`() {

        mockMvc.perform(
            get("/api/v1/contactmoment/kanaal")
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andExpect(jsonPath("$.[0]").value("MAIL"))
            .andReturn()
    }

}
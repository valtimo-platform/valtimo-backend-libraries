/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.plugin.web.rest

import com.ritense.plugin.BaseIntegrationTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets


internal class PluginDefinitionResourceIT: BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should get plugin action definitions by key`() {
        mockMvc.perform(get("/api/plugin/definition/test-plugin/action")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(4)))
            .andExpectActionInResponse("test-action", "Test action",
                "This is an action used to verify plugin framework functionality")
            .andExpectActionInResponse("other-test-action", "Test action 2",
                "This is an action used to test method overloading")
            .andExpectActionInResponse("child-override-test-action", "Override test action",
                "This is an action used to test method inheritance")
            .andExpectActionInResponse("parent-test-action", "Parent test action",
                "This is an action used to test method inheritance")
    }

    @Test
    fun `should get plugin action definitions by key and activity type`() {
        mockMvc.perform(get("/api/plugin/definition/test-plugin/action?activityType=USER_TASK")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(3)))
            .andExpectActionInResponse("test-action", "Test action",
                "This is an action used to verify plugin framework functionality")
            .andExpectActionInResponse("other-test-action", "Test action 2",
                "This is an action used to test method overloading")
            .andExpectActionInResponse("parent-test-action", "Parent test action",
                "This is an action used to test method inheritance")
    }

    private fun ResultActions.andExpectActionInResponse(
        key: String,
        title: String,
        description: String
    ): ResultActions {
        return this.andExpect(jsonPath("$.[?(" +
            "@.key == \"$key\" && " +
            "@.title == \"$title\" && " +
            "@.description == \"$description\"" +
            ")]").exists())
    }
}
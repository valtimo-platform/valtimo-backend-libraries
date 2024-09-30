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

package com.ritense.logging

import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
class LoggingContextExceptionTranslatorIT : BaseIntegrationTest() {

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
    fun `should log properties for an exception`() {
        mockMvc.perform(get("/api/v1/test-error"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)

        val errorEvent = loggingEventRepository.findAll(byLevel("ERROR")).last()

        assertEquals("Internal Server Error", errorEvent.formattedMessage)
        assertTrue(errorEvent.getStacktrace()!!.startsWith("java.lang.IllegalStateException: outer-test-error"))
        assertTrue(errorEvent.getStacktrace()!!.contains("Caused by: java.lang.IllegalStateException: inner-test-error"))
        assertEquals(2, errorEvent.properties.size)
        assertTrue(errorEvent.properties.map { it.getKey() }.contains("inner key"))
        assertTrue(errorEvent.properties.map { it.value }.contains("inner value"))
        assertTrue(errorEvent.properties.map { it.getKey() }.contains("outer key"))
        assertTrue(errorEvent.properties.map { it.value }.contains("outer value"))
    }
}
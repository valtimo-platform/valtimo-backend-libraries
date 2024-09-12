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

package com.ritense.valtimo.logging

import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.contract.LoggingConstants
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.logging.impl.TestController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets
import java.util.UUID

class UserLoggingFilterIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var userLoggingFilter: UserLoggingFilter

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .addFilters<DefaultMockMvcBuilder>(userLoggingFilter)
            .build()

        TestController.listAppender.list.clear()
    }

    @Test
    @WithMockUser("henk@ritense.com")
    fun `should add user to MDC when set`() {
        val user = mock<ManageableUser>()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/test")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

        val messageList = TestController.listAppender.list
        // MDC should be empty after the request is done
        assertEquals(0, MDC.getCopyOfContextMap().size)

        assertEquals(1, messageList.size)
        // parse as UUID to check if the value is a valid UUID
        UUID.fromString(messageList[0].mdcPropertyMap[LoggingConstants.MDC_CORRELATION_ID_KEY])

        // check if the user is set in the MDC
        assertEquals("henk@ritense.com", messageList[0].mdcPropertyMap[LoggingConstants.MDC_USER_EMAIL_KEY])
    }

    @Test
    fun `should not user to MDC when no user set`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/test")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

        val messageList = TestController.listAppender.list
        // MDC should be empty after the request is done
        assertEquals(0, MDC.getCopyOfContextMap().size)

        assertEquals(1, messageList.size)
        // parse as UUID to check if the value is a valid UUID
        UUID.fromString(messageList[0].mdcPropertyMap[LoggingConstants.MDC_CORRELATION_ID_KEY])

        // check if the user is set in the MDC
        assertEquals(null, messageList[0].mdcPropertyMap[LoggingConstants.MDC_USER_EMAIL_KEY])
    }
}
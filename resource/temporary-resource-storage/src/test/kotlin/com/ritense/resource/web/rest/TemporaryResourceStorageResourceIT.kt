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

package com.ritense.resource.web.rest

import com.ritense.resource.BaseIntegrationTest
import com.ritense.resource.domain.TemporaryResourceUploadedEvent
import com.ritense.resource.service.TemporaryResourceStorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.PayloadApplicationEvent
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

internal class TemporaryResourceStorageResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var temporaryResourceStorageService: TemporaryResourceStorageService

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    @WithMockUser(USER_EMAIL)
    fun `should upload file with meta data`() {
        val file = MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            """Hello World!""".byteInputStream()
        )

        mockMvc.perform(
            multipart("/api/v1/resource/temp")
                .file(file)
                .param("author", "Klaveren")
                .contentType(MULTIPART_FORM_DATA_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)

        val captor = argumentCaptor<PayloadApplicationEvent<TemporaryResourceUploadedEvent>>()
        verify(applicationEventMulticaster, atLeastOnce()).multicastEvent(captor.capture(), any())
        val resourceId = captor.firstValue.payload.resourceId
        val resourceContent = temporaryResourceStorageService.getResourceContentAsInputStream(resourceId)
        val resourceMetaData = temporaryResourceStorageService.getResourceMetadata(resourceId)
        assertThat(resourceContent.bufferedReader().readText()).isEqualTo("Hello World!")
        assertThat(resourceMetaData).containsEntry("author", "Klaveren")
        assertThat(resourceMetaData).containsEntry("contentType", "text/plain")
        assertThat(resourceMetaData).containsEntry("filename", "hello.txt")
        assertThat(resourceMetaData).containsEntry("user", USER_EMAIL)
    }

    companion object {
        private const val USER_EMAIL = "user@valtimo.nl"
    }
}

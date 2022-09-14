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

package com.ritense.resource.web.rest

import com.ritense.resource.BaseIntegrationTest
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.resource.web.rest.response.ResourceDto
import com.ritense.valtimo.contract.json.Mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.mock.web.MockMultipartFile
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
    fun `should upload file with meta data`() {
        val file = MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            """Hello World!""".byteInputStream()
        )

        val response = mockMvc.perform(
            multipart("/api/resource/temp")
                .file(file)
                .param("author", "Klaveren")
                .contentType(MULTIPART_FORM_DATA_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
            .response

        val resource = Mapper.INSTANCE.get().readValue(response.contentAsString, ResourceDto::class.java)
        val resourceContent = temporaryResourceStorageService.getResourceContentAsInputStream(resource.id)
        val resourceMetaData = temporaryResourceStorageService.getResourceMetadata(resource.id)
        assertThat(resourceContent.bufferedReader().readText()).isEqualTo("Hello World!")
        assertThat(resourceMetaData).containsEntry("author", "Klaveren")
        assertThat(resourceMetaData).containsEntry("FILE_NAME", "hello.txt")
        assertThat(resourceMetaData).containsEntry("CONTENT_TYPE", "text/plain")
    }
}

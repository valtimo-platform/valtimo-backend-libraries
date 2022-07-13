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
package com.ritense.smartdocuments.web.rest

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.service.PluginService
import com.ritense.smartdocuments.plugin.SmartDocumentsPlugin
import com.ritense.smartdocuments.plugin.SmartDocumentsPluginFactory
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

internal class SmartDocumentsDemoResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var pluginService: PluginService
    lateinit var runtimeService: RuntimeService
    lateinit var smartDocumentsDemoResource: SmartDocumentsDemoResource

    @BeforeEach
    fun init() {
        pluginService = mock()
        runtimeService = mock()
        smartDocumentsDemoResource = SmartDocumentsDemoResource(pluginService, runtimeService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(smartDocumentsDemoResource)
            .build()
    }

    @Test
    fun `should retrieve plugin to generate document`() {
        whenever(runtimeService.getVariables("ad12510e-ee17-11ec-b4fd-fad19e608849"))
            .thenReturn(mapOf("age" to 18))

        mockMvc.perform(
            post("/api/smart-documents/demo/generate")
                .param("processInstanceId", "ad12510e-ee17-11ec-b4fd-fad19e608849")
                .param("pluginConfigurationId", "5152abe3-59d7-430b-98b6-05ff1209d7c5")
                .param("templateGroup", "test-template-group")
                .param("templateName", "test-template-name")
                .param("format", "PDF")
                .param("leeftijd", "pv:age")
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
    }
}

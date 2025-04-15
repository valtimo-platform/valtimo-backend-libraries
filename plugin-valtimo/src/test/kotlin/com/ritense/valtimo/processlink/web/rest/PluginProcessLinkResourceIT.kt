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

package com.ritense.valtimo.processlink.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.valtimo.BaseIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
@Import(ProcessLinkActivityService::class)
internal class PluginProcessLinkResourceIT @Autowired constructor(
    private val webApplicationContext: WebApplicationContext
) : BaseIntegrationTest() {
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should list compatible plugin process link configurations for a certain plugin action type`() {
        runWithoutAuthorization {
            mockMvc.perform(
                get("/api/v1/process-link/plugin?pluginActionDefinitionKey=$PLUGIN_ACTION_DEFINITION_KEY")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].processDefinitionKey").isNotEmpty)
                .andExpect(jsonPath("$[1].processDefinitionKey").isNotEmpty)
                .andExpect(jsonPath("$[0].versions").isArray)
                .andExpect(jsonPath("$[0].versions.length()").value(1))
                .andExpect(jsonPath("$[1].versions").isArray)
                .andExpect(jsonPath("$[1].versions.length()").value(1))
                .andExpect(jsonPath("$[0].versions[0].processLinks").isArray)
                .andExpect(jsonPath("$[1].versions[0].processLinks").isArray)
                .andExpect(jsonPath("$[0].versions[0].processLinks.length()").value(2))
                .andExpect(jsonPath("$[1].versions[0].processLinks.length()").value(2))
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].id").isNotEmpty)
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].activityId").isNotEmpty)
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].activityType").isNotEmpty)
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].processLinkType").value("plugin"))
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].pluginConfigurationId").isNotEmpty)
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].pluginActionDefinitionKey").value("create-portaaltaak"))
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].actionProperties.formType").value("id"))
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].actionProperties.formTypeId").value("person"))
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].actionProperties.sendData").isArray)
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].actionProperties.receiveData").isArray)
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].actionProperties.receiver").value("other"))
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].actionProperties.identificationKey").value("bsn"))
                .andExpect(jsonPath("$[0].versions[0].processLinks[0].actionProperties.identificationValue").value("569312863"))
        }
    }

    @Test
    fun `should return empty list when no process links match the given plugin action definition key`() {
        runWithoutAuthorization {
            mockMvc.perform(
                get("/api/v1/process-link/plugin?pluginActionDefinitionKey=$INVALID_PLUGIN_ACTION_DEFINITION_KEY")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(0))
        }
    }

    companion object {
        const val PLUGIN_ACTION_DEFINITION_KEY = "create-portaaltaak"
        const val INVALID_PLUGIN_ACTION_DEFINITION_KEY = "invalid_key"
    }
}
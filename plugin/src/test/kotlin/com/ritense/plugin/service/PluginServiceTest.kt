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

package com.ritense.plugin.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PluginServiceTest {

    lateinit var pluginDefinitionRepository: PluginDefinitionRepository
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository
    lateinit var pluginService: PluginService

    @BeforeEach
    fun init() {
        pluginDefinitionRepository = mock()
        pluginConfigurationRepository = mock()
        pluginService = PluginService(pluginDefinitionRepository, pluginConfigurationRepository)
    }

    @Test
    fun `should get plugin definitions from repository`(){
        pluginService.getPluginDefinitions()
        verify(pluginDefinitionRepository).findAll()
    }

    @Test
    fun `should get plugin configurations from repository`(){
        pluginService.getPluginConfigurations()
        verify(pluginConfigurationRepository).findAll()
    }

    @Test
    fun `should save plugin configuration`(){
        val pluginDefinition = PluginDefinition("key", "title", "description", "className")
        val pluginConfiguration = PluginConfiguration("key", "title", "description", pluginDefinition)

        whenever(pluginDefinitionRepository.getById("key")).thenReturn(pluginDefinition)
        whenever(pluginConfigurationRepository.save(any())).thenReturn(pluginConfiguration)

        pluginService
            .createPluginConfiguration(
                "key", "title", "{\"name\": \"whatever\" }", "key"
            )
        verify(pluginConfigurationRepository).save(any())
    }

}
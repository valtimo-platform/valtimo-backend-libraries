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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PluginServiceTest {

    lateinit var pluginDefinitionRepository: PluginDefinitionRepository
    lateinit var pluginService: PluginService

    @BeforeEach
    fun init() {
        pluginDefinitionRepository = mock()
        pluginService = PluginService(pluginDefinitionRepository)
    }

    @Test
    fun `should get plugin definitions from repository`(){
        pluginService.getPluginDefinitions()
        verify(pluginDefinitionRepository).findAll()
    }

}
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

package com.ritense.plugin

import com.ritense.plugin.autodeployment.AutoDeploymentTestPlugin
import com.ritense.plugin.autodeployment.TestAutoDeploymentPluginFactory
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.configuration.ProcessLinkAutoConfiguration
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.mockito.service.RuntimeServiceFluentMock
import org.mockito.kotlin.spy
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = [ProcessLinkAutoConfiguration::class])
class TestApplication {

    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testPlugin(pluginService: PluginService): PluginFactory<TestPlugin> {
            return spy(TestPluginFactory("someString", pluginService))
        }

        @Bean
        fun testAutoDeploymentPlugin(pluginService: PluginService): PluginFactory<AutoDeploymentTestPlugin> {
            return spy(TestAutoDeploymentPluginFactory("whoCares",pluginService))
        }

        @Bean
        fun testCategoryPlugin(pluginService: PluginService): PluginFactory<TestCategoryPlugin> {
            return spy(TestCategoryPluginFactory(pluginService))
        }

        @Bean
        fun runtimeService():RuntimeService {
            return RuntimeServiceFluentMock().runtimeService
        }
    }
}

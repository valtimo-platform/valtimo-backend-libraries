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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.BaseIntegrationTest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.InvocationTargetException
import java.util.UUID
import javax.persistence.EntityManager
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


internal class PluginServiceIT: BaseIntegrationTest() {
    @Autowired
    lateinit var pluginService: PluginService

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Autowired
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    lateinit var pluginConfiguration: PluginConfiguration

    @BeforeEach
    fun init() {
        val pluginDefinition = pluginDefinitionRepository.getById("test-plugin");
        pluginConfiguration = pluginConfigurationRepository.save(PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            null,
            pluginDefinition
        ))
    }

    @Test
    @Transactional
    fun `should be able to save configuration with encypted property and decrypt on load`() {
        val input = """
            {
                "property1": "test123",
                "property2": false,
                "property3": 123
            }
        """.trimMargin()

        val configuration = pluginService.createPluginConfiguration(
            "title",
            Mapper.INSTANCE.get().readTree(input),
            "test-plugin"
        )

        // value should be decrypted when loading from database
        val configurations = pluginService.getPluginConfigurations()
        val configurationFromDatabase = configurations.filter { it.id.id == configuration.id.id }.first()

        assertEquals("test123", configurationFromDatabase.properties!!.get("property1").textValue())
    }

    @Test
    @Transactional
    fun `should invoke an action on the plugin`() {
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            processDefinitionId = UUID.randomUUID().toString(),
            activityId = "test",
            pluginConfigurationId = pluginConfiguration.id,
            pluginActionDefinitionKey = "other-test-action",
            actionProperties = Mapper.INSTANCE.get().readTree("""{"someString": "test123"}""")
        )
        pluginService.invoke(String(), processLink)
    }

    @Test
    @Transactional
    fun `should fail when invoking an action with missing required parameter`() {
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            processDefinitionId = UUID.randomUUID().toString(),
            activityId = "test",
            pluginConfigurationId = pluginConfiguration.id,
            pluginActionDefinitionKey = "other-test-action"
        )

        assertFailsWith<InvocationTargetException>(
            block = {
                pluginService.invoke(String(), processLink)
            }
        )
    }
}
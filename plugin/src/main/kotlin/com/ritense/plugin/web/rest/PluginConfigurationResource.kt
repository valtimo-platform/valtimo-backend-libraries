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

package com.ritense.plugin.web.rest

import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.request.CreatePluginConfigurationDto
import com.ritense.plugin.web.rest.request.UpdatePluginConfigurationDto
import com.ritense.plugin.web.rest.result.PluginConfigurationDto
import com.ritense.plugin.web.rest.result.PluginConfigurationExportDto
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.operaton.bpm.engine.repository.ProcessDefinition
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Comparator.comparingInt
import java.util.UUID

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class PluginConfigurationResource(
    private var pluginService: PluginService
) {

    @GetMapping("/v1/plugin/configuration")
    fun getPluginDefinitions(
        @LoggableResource(resourceType = ProcessDefinition::class) @RequestParam("pluginDefinitionKey") pluginDefinitionKey: String?,
        @RequestParam("pluginConfigurationTitle") pluginConfigurationTitle: String?,
        @RequestParam("category") category: String?,
        @RequestParam("activityType") activityType: ActivityTypeWithEventName?
    )
        : ResponseEntity<List<PluginConfigurationDto>> {

        return ResponseEntity.ok(
            pluginService.getPluginConfigurations(
                PluginConfigurationSearchParameters(
                    pluginDefinitionKey = pluginDefinitionKey,
                    pluginConfigurationTitle = pluginConfigurationTitle,
                    category = category,
                    activityType = activityType
                )
            )
                .map { PluginConfigurationDto(it) })
    }

    @PostMapping("/v1/plugin/configuration")
    fun createPluginConfiguration(
        @RequestBody createPluginConfiguration: CreatePluginConfigurationDto
    ): ResponseEntity<PluginConfigurationDto> {
        return withLoggingContext(PluginConfiguration::class, createPluginConfiguration.id) {
            val pluginConfigurationId = if (createPluginConfiguration.id == null) {
                PluginConfigurationId.newId()
            } else {
                PluginConfigurationId.existingId(createPluginConfiguration.id)
            }

            ResponseEntity.ok(
                PluginConfigurationDto(
                    pluginService.createPluginConfiguration(
                        pluginConfigurationId,
                        createPluginConfiguration.title,
                        createPluginConfiguration.properties,
                        createPluginConfiguration.definitionKey
                    )
                )
            )
        }
    }

    @PutMapping("/v1/plugin/configuration/{pluginConfigurationId}")
    fun updatePluginConfiguration(
        @LoggableResource(resourceType = PluginConfiguration::class) @PathVariable(name = "pluginConfigurationId") pluginConfigurationId: UUID,
        @RequestBody updatePluginConfiguration: UpdatePluginConfigurationDto
    ): ResponseEntity<PluginConfigurationDto> {
        val newPluginConfigurationId = if (updatePluginConfiguration.newId == null) {
            PluginConfigurationId.existingId(pluginConfigurationId)
        } else {
            PluginConfigurationId(updatePluginConfiguration.newId)
        }

        return ResponseEntity.ok(
            PluginConfigurationDto(
                pluginService.updatePluginConfiguration(
                    PluginConfigurationId.existingId(pluginConfigurationId),
                    newPluginConfigurationId,
                    updatePluginConfiguration.title,
                    updatePluginConfiguration.properties
                )
            )
        )
    }

    @GetMapping("/v1/plugin/configuration/export")
    fun exportPluginConfiguration(): ResponseEntity<List<PluginConfigurationExportDto>> {
        val pluginConfigurations = pluginService.getPluginConfigurations(PluginConfigurationSearchParameters())
            .sortedWith(comparingInt<PluginConfiguration> { pluginConfiguration ->
                pluginConfiguration.properties?.fieldNames()?.asSequence()?.count { it.contains("PluginConfiguration") }
                    ?: 0
            }
                .thenBy { it.pluginDefinition.key }
                .thenBy { it.title })
            .map { PluginConfigurationExportDto.of(it) }
        return ResponseEntity.ok(pluginConfigurations)
    }

    @DeleteMapping("/v1/plugin/configuration/{pluginConfigurationId}")
    fun deletePluginConfiguration(
        @LoggableResource(resourceType = PluginConfiguration::class) @PathVariable(name = "pluginConfigurationId") pluginConfigurationId: UUID
    ): ResponseEntity<Void> {
        pluginService.deletePluginConfiguration(PluginConfigurationId.existingId(pluginConfigurationId))
        return ResponseEntity.noContent().build()
    }
}

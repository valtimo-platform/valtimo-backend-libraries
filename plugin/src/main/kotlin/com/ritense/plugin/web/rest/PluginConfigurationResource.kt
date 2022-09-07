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

package com.ritense.plugin.web.rest

import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.request.CreatePluginConfigurationDto
import com.ritense.plugin.web.rest.request.UpdatePluginConfigurationDto
import com.ritense.plugin.web.rest.result.PluginConfigurationDto
import org.springframework.http.MediaType
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
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/plugin"])
class PluginConfigurationResource(
    private var pluginService: PluginService
) {

    @GetMapping(value = ["/configuration"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPluginDefinitions(@RequestParam("category") category: String?,
                             @RequestParam("activityType") activityType: ActivityType?)
        : ResponseEntity<List<PluginConfigurationDto>> {

        return ResponseEntity.ok(
            pluginService.getPluginConfigurations(
                PluginConfigurationSearchParameters(
                    category,
                    activityType
                )
            )
            .map { PluginConfigurationDto(it) })
    }

    @PostMapping(value = ["/configuration"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createPluginConfiguration(
        @RequestBody createPluginConfiguration: CreatePluginConfigurationDto
    ): ResponseEntity<PluginConfigurationDto> {
        return ResponseEntity.ok(
            PluginConfigurationDto(
                pluginService.createPluginConfiguration(
                    createPluginConfiguration.title,
                    createPluginConfiguration.properties,
                    createPluginConfiguration.definitionKey
                )
            )
        )
    }

    @PutMapping(value = ["/configuration/{pluginConfigurationId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updatePluginConfiguration(
        @PathVariable(name = "pluginConfigurationId") pluginConfigurationId: UUID,
        @RequestBody updatePluginConfiguration: UpdatePluginConfigurationDto
    ): ResponseEntity<PluginConfigurationDto> {
        return ResponseEntity.ok(
            PluginConfigurationDto(
                pluginService.updatePluginConfiguration(
                    PluginConfigurationId.existingId(pluginConfigurationId),
                    updatePluginConfiguration.title,
                    updatePluginConfiguration.properties
                )
            )
        )
    }

    @DeleteMapping(value = ["/configuration/{pluginConfigurationId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deletePluginConfiguration(
        @PathVariable(name = "pluginConfigurationId") pluginConfigurationId: UUID
    ): ResponseEntity<Void> {
        pluginService.deletePluginConfiguration(PluginConfigurationId.existingId(pluginConfigurationId))
        return ResponseEntity.noContent().build()
    }
}
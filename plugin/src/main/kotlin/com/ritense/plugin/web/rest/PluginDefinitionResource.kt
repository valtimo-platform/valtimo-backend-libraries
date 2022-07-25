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
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.dto.PluginActionDefinitionDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/plugin"])
class PluginDefinitionResource(
    private var pluginService: PluginService
) {

    @GetMapping(value = ["/definition"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPluginDefinitions(): ResponseEntity<List<PluginDefinition>> {
        return ResponseEntity.ok(pluginService.getPluginDefinitions())
    }

    @GetMapping(value = ["/definition/{pluginDefinitionKey}/action"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPluginDefinitionActions(
        @PathVariable pluginDefinitionKey: String,
        @RequestParam("activityType") activityType: ActivityType?
    ): ResponseEntity<List<PluginActionDefinitionDto>> {
        return ResponseEntity.ok(
            pluginService.getPluginDefinitionActions(pluginDefinitionKey, activityType).map {
                PluginActionDefinitionDto(
                    it.id.key,
                    it.title,
                    it.description
                )
            }
        )
    }
}
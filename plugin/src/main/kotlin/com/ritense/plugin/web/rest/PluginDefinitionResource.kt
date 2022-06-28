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
import java.util.UUID

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
        return ResponseEntity.ok(pluginService.getPluginDefinitionActions(pluginDefinitionKey, activityType))
    }
}
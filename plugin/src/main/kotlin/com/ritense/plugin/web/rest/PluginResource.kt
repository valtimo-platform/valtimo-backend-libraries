package com.ritense.plugin.web.rest

import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// TODO Do we want to rename this to PluginDefinitionResource?
@RestController
@RequestMapping(value = ["/api/plugin"])
class PluginResource(
    private var pluginService: PluginService
) {

    @GetMapping(value = ["/definition"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPluginDefinitions(@RequestParam("activityType") activityType: String?): ResponseEntity<MutableList<PluginDefinition>> {
        return ResponseEntity.ok(pluginService.getPluginDefinitions())
    }
}
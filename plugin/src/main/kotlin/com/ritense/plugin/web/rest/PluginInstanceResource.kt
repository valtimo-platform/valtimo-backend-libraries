package com.ritense.plugin.web.rest

import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/plugin"])
class PluginInstanceResource(
    private var pluginService: PluginService
) {

    @GetMapping(value = ["/configuration"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPluginDefinitions(): ResponseEntity<List<PluginConfiguration>> {
        return ResponseEntity.ok(pluginService.getPluginConfigurations())
    }

    @PostMapping(value = ["/configuration"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createPluginConfiguration(
        @RequestBody pluginConfiguration: com.ritense.plugin.web.rest.dto.PluginConfiguration
    ): ResponseEntity<PluginConfiguration> {
        return ResponseEntity.ok(
            pluginService.createPluginConfiguration(
                pluginConfiguration.title,
                pluginConfiguration.properties,
                pluginConfiguration.definitionKey
            )
        )
    }
}
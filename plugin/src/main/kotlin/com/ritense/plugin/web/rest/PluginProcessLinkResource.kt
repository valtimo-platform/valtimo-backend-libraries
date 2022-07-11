package com.ritense.plugin.web.rest

import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkCreateDto
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkResultDto
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/process-link"])
class PluginProcessLinkResource(
    private var pluginService: PluginService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProcessLinks(
        @RequestParam("processDefinitionId") processDefinitionId: String,
        @RequestParam("activityId") activityId: String
    ): ResponseEntity<List<PluginProcessLinkResultDto>> {
        val list: List<PluginProcessLinkResultDto> = pluginService.getProcessLinks(processDefinitionId, activityId)

        return ResponseEntity.ok(list)
    }

    @PostMapping
    fun createProcessLink(
        @RequestBody processLink: PluginProcessLinkCreateDto
    ): ResponseEntity<Unit> {
        pluginService.createProcessLink(processLink)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PutMapping
    fun updateProcessLink(
        @RequestBody processLink: PluginProcessLinkUpdateDto
    ): ResponseEntity<Unit> {
        pluginService.updateProcessLink(processLink)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.processlink.web.rest

import com.ritense.processlink.exception.ProcessLinkNotFoundException
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class ProcessLinkTaskResource(
    private var processLinkActivityService: ProcessLinkActivityService
) {
    @GetMapping(value = ["/v2/process-link/task/{taskId}"])
    fun getTask(@PathVariable taskId: UUID): ResponseEntity<ProcessLinkActivityResult<*>> {
        return try {
            ResponseEntity.ok(processLinkActivityService.openTask(taskId))
        } catch (e: ProcessLinkNotFoundException) {
            ResponseEntity.noContent().build()
        }}

    @GetMapping(value = ["/v1/process-definition/{processDefinitionId}/start-form"])
    fun getFormDefinition(@PathVariable processDefinitionId: UUID): ResponseEntity<Any> {
        return ResponseEntity.ok(processLinkActivityService.getStartEventObject(processDefinitionId.toString()))
    }
}

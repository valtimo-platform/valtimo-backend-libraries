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

package com.ritense.script.web.rest

import com.ritense.script.service.ScriptService
import com.ritense.script.web.rest.dto.DeleteScriptRequest
import com.ritense.script.web.rest.dto.ScriptContentDto
import com.ritense.script.web.rest.dto.ScriptDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class ScriptManagementResource(
    private val scriptService: ScriptService,
) {

    @GetMapping("/v1/script")
    fun getScript(
    ): ResponseEntity<List<ScriptDto>> {
        return ResponseEntity.ok(scriptService.getAllScripts().map {
            ScriptDto.of(it)
        })
    }

    @PostMapping("/v1/script")
    fun createScript(
        @RequestBody request: ScriptDto
    ): ResponseEntity<ScriptDto> {
        return ResponseEntity.ok(ScriptDto.of(scriptService.createScript(request.toScript())))
    }

    @DeleteMapping("/v1/script")
    fun deleteScripts(
        @RequestBody request: DeleteScriptRequest
    ): ResponseEntity<Unit> {
        scriptService.deleteScripts(request.scripts)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/v1/script/{key}/content")
    fun getScriptContent(
        @PathVariable key: String
    ): ResponseEntity<ScriptContentDto> {
        val script = scriptService.getScript(key)
        return ResponseEntity.ok(ScriptContentDto.of(script))
    }

    @PutMapping("/v1/script/{key}/content")
    fun updateScriptContent(
        @PathVariable key: String,
        @RequestBody request: ScriptContentDto,
    ): ResponseEntity<ScriptContentDto> {
        val script = scriptService.getScript(key)
        script.content = request.content
        scriptService.saveScript(script)
        return ResponseEntity.ok(ScriptContentDto.of(script))
    }
}

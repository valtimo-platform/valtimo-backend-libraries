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

package com.ritense.template.web.rest

import com.ritense.template.service.TemplateService
import com.ritense.template.web.rest.dto.DeleteTemplateRequest
import com.ritense.template.web.rest.dto.TemplateDto
import com.ritense.template.web.rest.dto.TemplateMetadataDto
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
class TemplateManagementResource(
    private val templateService: TemplateService,
) {

    @GetMapping("/v1/template")
    fun getTemplates(
    ): ResponseEntity<List<TemplateMetadataDto>> {
        return ResponseEntity.ok(templateService.getAllTemplates().map {
            TemplateMetadataDto.of(it)
        })
    }

    @GetMapping("/v1/template/{key}")
    fun getTemplate(
        @PathVariable key: String
    ): ResponseEntity<TemplateDto> {
        val template = templateService.getTemplate(key)
        return ResponseEntity.ok(TemplateDto.of(template))
    }

    @PostMapping("/v1/template")
    fun createTemplate(
        @RequestBody request: TemplateMetadataDto
    ): ResponseEntity<TemplateMetadataDto> {
        return ResponseEntity.ok(TemplateMetadataDto.of(templateService.createTemplate(request.toTemplate())))
    }

    @PutMapping("/v1/template")
    fun updateTemplate(
        @RequestBody request: TemplateDto,
    ): ResponseEntity<TemplateDto> {
        val template = templateService.updateTemplate(request.key, request.content)
        return ResponseEntity.ok(TemplateDto.of(template))
    }

    @DeleteMapping("/v1/template")
    fun deleteTemplates(
        @RequestBody request: DeleteTemplateRequest
    ): ResponseEntity<Unit> {
        templateService.deleteTemplates(request.templates)
        return ResponseEntity.ok().build()
    }
}

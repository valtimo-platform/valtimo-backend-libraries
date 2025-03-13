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

package com.ritense.formflow.web.rest

import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.importer.FormFlowDefinitionImporter
import com.ritense.formflow.service.FormFlowService
import com.ritense.formflow.web.rest.result.FormFlowDefinitionDto
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
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
class FormFlowManagementResource(
    private val formFlowService: FormFlowService,
    private val formFlowDefinitionImporter: FormFlowDefinitionImporter
) {
    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition")
    @Transactional
    fun getAllFormFlowDefinitions(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<Page<FormFlowDefinitionDto>> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, versionTag)
        val definitions = formFlowService.getFormFlowDefinitions(caseDefinitionId, pageable)
            .map { FormFlowDefinitionDto.of(it, formFlowDefinitionImporter.isAutoDeployed(it.id.key)) }
        return ResponseEntity.ok(definitions)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition/{definitionKey}")
    @Transactional
    fun getFormFlowDefinitionById(
        @LoggableResource("formFlowDefinitionKey") @PathVariable definitionKey: String,
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
    ): ResponseEntity<FormFlowDefinitionDto> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, versionTag)
        val definition = formFlowService.findDefinition(FormFlowDefinitionId(definitionKey, caseDefinitionId))
        val readOnly = formFlowDefinitionImporter.isAutoDeployed(definition.id.key)
        return ResponseEntity.ok(FormFlowDefinitionDto.of(definition, readOnly))
    }

    @DeleteMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition/{definitionKey}")
    @Transactional
    fun deleteFormFlowDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @LoggableResource("formFlowDefinitionKey") @PathVariable definitionKey: String,
    ): ResponseEntity<Unit> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, versionTag)
        if (formFlowDefinitionImporter.isAutoDeployed(definitionKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        formFlowService.deleteByKeyAndsCaseDefinition(definitionKey, caseDefinitionId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition")
    @Transactional
    fun createFormFlowDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @RequestBody definitionDto: FormFlowDefinitionDto
    ): ResponseEntity<FormFlowDefinitionDto> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, versionTag)
        if (formFlowService.findDefinitionOrNull(definitionDto.key, caseDefinitionId) != null) {
            return ResponseEntity.badRequest().build()
        }
        val newDefinition = formFlowService.save(definitionDto.toEntity(caseDefinitionId))
        return ResponseEntity.ok(FormFlowDefinitionDto.of(newDefinition, false))
    }

    @PutMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition/{definitionKey}")
    @Transactional
    fun updateFormFlowDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @LoggableResource("formFlowDefinitionKey") @PathVariable definitionKey: String,
        @RequestBody definitionDto: FormFlowDefinitionDto
    ): ResponseEntity<FormFlowDefinitionDto> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, versionTag)
        val readOnly = formFlowDefinitionImporter.isAutoDeployed(definitionKey)
        if (readOnly) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val newDefinition = formFlowService.save(definitionDto.toEntity(caseDefinitionId))
        return ResponseEntity.ok(FormFlowDefinitionDto.of(newDefinition, false))
    }
}

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

package com.ritense.valtimo.formflow.web.rest

import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.service.FormFlowDeploymentService
import com.ritense.formflow.service.FormFlowService
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.formflow.web.rest.result.FormFlowDefinitionDto
import com.ritense.valtimo.formflow.web.rest.result.ListFormFlowDefinitionResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
    private val formFlowDeploymentService: FormFlowDeploymentService
) {
    @GetMapping("/v1/form-flow/definition")
    @Transactional
    fun getAllFormFlowDefinitions(
    ): ResponseEntity<Page<ListFormFlowDefinitionResponse>> {
        val definitions = formFlowService.getFormFlowDefinitions()
            .groupBy { it.id.key }
            .map { ListFormFlowDefinitionResponse.of(it.value, formFlowDeploymentService.isAutoDeployed(it.value.first().id.key)) }
            .sortedBy { it.key }
        return ResponseEntity.ok(PageImpl(definitions))
    }

    @GetMapping("/v1/form-flow/definition/{definitionKey}/{definitionVersion}")
    @Transactional
    fun getFormFlowDefinitionById(
        @LoggableResource("formFlowDefinitionKey") @PathVariable definitionKey: String,
        @PathVariable definitionVersion: Long,
    ): ResponseEntity<FormFlowDefinitionDto> {
        val definition = formFlowService.findDefinition(FormFlowDefinitionId(definitionKey, definitionVersion))
        val readOnly = formFlowDeploymentService.isAutoDeployed(definition.id.key)
        return ResponseEntity.ok(FormFlowDefinitionDto.of(definition, readOnly))
    }

    @DeleteMapping("/v1/form-flow/definition/{definitionKey}")
    @Transactional
    fun deleteFormFlowDefinition(
        @LoggableResource("formFlowDefinitionKey") @PathVariable definitionKey: String,
    ): ResponseEntity<Unit> {
        if (formFlowDeploymentService.isAutoDeployed(definitionKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        formFlowService.deleteByKey(definitionKey)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/v1/form-flow/definition")
    @Transactional
    fun createFormFlowDefinition(
        @RequestBody definitionDto: FormFlowDefinitionDto
    ): ResponseEntity<FormFlowDefinitionDto> {
        if (formFlowService.findLatestDefinitionByKey(definitionDto.key) != null) {
            return ResponseEntity.badRequest().build()
        }
        val newDefinition = formFlowService.save(definitionDto.toEntity())
        return ResponseEntity.ok(FormFlowDefinitionDto.of(newDefinition, false))
    }

    @PutMapping("/v1/form-flow/definition/{definitionKey}")
    @Transactional
    fun updateFormFlowDefinition(
        @LoggableResource("formFlowDefinitionKey") @PathVariable definitionKey: String,
        @RequestBody definitionDto: FormFlowDefinitionDto
    ): ResponseEntity<FormFlowDefinitionDto> {
        val readOnly = formFlowDeploymentService.isAutoDeployed(definitionKey)
        if (readOnly) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val oldDefinition = formFlowService.findLatestDefinitionByKey(definitionDto.key)
            ?: return ResponseEntity.notFound().build()
        if (definitionDto.version != oldDefinition.id.version + 1) {
            return ResponseEntity.badRequest().build()
        }

        val newDefinition = formFlowService.save(definitionDto.toEntity())
        return ResponseEntity.ok(FormFlowDefinitionDto.of(newDefinition, false))
    }

}

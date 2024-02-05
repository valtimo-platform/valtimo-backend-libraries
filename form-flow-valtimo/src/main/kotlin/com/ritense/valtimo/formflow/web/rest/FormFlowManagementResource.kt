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

package com.ritense.valtimo.formflow.web.rest

import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.formflow.web.rest.result.FormFlowDefinitionResponse
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class FormFlowManagementResource(
    private val formFlowService: FormFlowService
) {
    @GetMapping("/management/v1/form-flow/definition")
    @Transactional
    fun getAllFormFlowDefinitions(
    ): ResponseEntity<List<FormFlowDefinitionResponse>> {
        val definitions = formFlowService.getFormFlowDefinitions()
            .groupBy { it.id.key }
            .map { FormFlowDefinitionResponse.of(it.value) }
        return ResponseEntity.ok(definitions)
    }

    @GetMapping("/management/v1/form-flow/definition/{definitionKey}")
    @Transactional
    fun getFormFlowDefinitionByKey(
        @PathVariable definitionKey: String,
    ): ResponseEntity<FormFlowDefinitionResponse> {
        val definition = formFlowService.findDefinition(definitionKey)
        return ResponseEntity.ok(FormFlowDefinitionResponse.of(definition))
    }

}

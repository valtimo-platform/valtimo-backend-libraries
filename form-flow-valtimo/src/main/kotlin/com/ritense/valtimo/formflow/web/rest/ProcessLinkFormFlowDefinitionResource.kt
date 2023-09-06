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
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowDefinition
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class ProcessLinkFormFlowDefinitionResource(
    val formFlowService: FormFlowService
) {

    @GetMapping(value = ["/v1/form-flow/definition", "/v1/process-link/form-flow-definition"]) // TODO: deprecate "/v1/process-link/form-flow-definition"
    fun getFormLinkOptions(): ResponseEntity<List<FormFlowDefinition>> {
        val formFlowDefinitions = formFlowService.getFormFlowDefinitions()

        val versionedDefinitions = formFlowDefinitions
            .map { formFlowDefinition -> FormFlowDefinition(formFlowDefinition.id.key, formFlowDefinition.id.version) }

        val latestDefinitions = formFlowDefinitions
            .distinctBy { formFlowDefinition -> formFlowDefinition.id.key }
            .map { formFlowDefinition -> FormFlowDefinition(formFlowDefinition.id.key) }

        return ResponseEntity.ok(versionedDefinitions + latestDefinitions)
    }
}

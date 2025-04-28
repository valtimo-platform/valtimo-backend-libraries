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

package com.ritense.valtimo.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.decision.CamundaDecisionService
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.repository.DecisionDefinition
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream

@RestController
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class DecisionManagementResource(
    private val camundaProcessService: CamundaProcessService,
    private val camundaDecisionService: CamundaDecisionService,
) {

    @GetMapping(
        value = ["/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/decision-definition"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun listDecisionDefinition(
        @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable(name = "caseDefinitionVersionTag") caseDefinitionVersionTag: String,
        @RequestPart(name = "file") dmn: MultipartFile?
    ): ResponseEntity<List<DecisionDefinition>> {

        val decisionDefinitions = runWithoutAuthorization {
            camundaDecisionService.getDecisionDefinitions(CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag))
        }

        return ResponseEntity.ok(decisionDefinitions)
    }

    @PostMapping(
        value = ["/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/decision-definition"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Transactional
    fun deployDecisionDefinition(
        @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable(name = "caseDefinitionVersionTag") caseDefinitionVersionTag: String,
        @RequestPart(name = "file") dmn: MultipartFile
    ): ResponseEntity<Any> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)
        val correctFileExtension = dmn.originalFilename?.endsWith(".dmn") == true

        if (!correctFileExtension) {
            return ResponseEntity.badRequest().body("Invalid file name. Must have '.dmn' suffix.")
        }

        runWithoutAuthorization {
            camundaProcessService.deploy(
                caseDefinitionId,
                dmn.originalFilename,
                ByteArrayInputStream(dmn.bytes),
                true,
                false
            )
        }

        return ResponseEntity.noContent().build()
    }

    @DeleteMapping(
        value = ["/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/decision-definition/{decisionDefinitionKey}"],
    )
    @Transactional
    fun deleteDecisionDefinition(
        @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable(name = "caseDefinitionVersionTag") caseDefinitionVersionTag: String,
        @PathVariable(name = "decisionDefinitionKey") decisionDefinitionKey: String,
    ): ResponseEntity<Any> {

        runWithoutAuthorization {
            camundaDecisionService.deleteDecisionDefinition(
                CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag),
                decisionDefinitionKey
            )
        }

        return ResponseEntity.noContent().build()
    }
}
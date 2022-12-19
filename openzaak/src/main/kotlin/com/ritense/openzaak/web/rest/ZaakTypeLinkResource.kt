/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.web.rest

import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.result.CreateServiceTaskHandlerResult
import com.ritense.openzaak.service.result.CreateZaakTypeLinkResult
import com.ritense.openzaak.service.result.ModifyServiceTaskHandlerResult
import com.ritense.openzaak.service.result.RemoveServiceTaskHandlerResult
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
interface ZaakTypeLinkResource {

    @GetMapping(value = ["/v1/openzaak/link/{documentDefinitionName}"])
    fun get(@PathVariable(name = "documentDefinitionName") documentDefinitionName: String): ResponseEntity<ZaakTypeLink?>

    @GetMapping(value = ["/v1/openzaak/link/process/{processDefinitionKey}"])
    fun getByProcess(@PathVariable(name = "processDefinitionKey") processDefinitionKey: String): ResponseEntity<List<ZaakTypeLink?>>

    @PostMapping("/v1/openzaak/link")
    fun create(@Valid @RequestBody request: CreateZaakTypeLinkRequest): ResponseEntity<CreateZaakTypeLinkResult>

    @DeleteMapping(value = ["/v1/openzaak/link/{documentDefinitionName}"])
    fun remove(@PathVariable(name = "documentDefinitionName") documentDefinitionName: String): ResponseEntity<ZaakTypeLink?>

    @PostMapping(value = ["/v1/openzaak/link/{id}/service-handler"])
    fun createServiceTaskHandler(
        @PathVariable(name = "id") id: UUID,
        @Valid @RequestBody request: ServiceTaskHandlerRequest
    ): ResponseEntity<CreateServiceTaskHandlerResult>

    @PutMapping(value = ["/v1/openzaak/link/{id}/service-handler"])
    fun modifyServiceTaskHandler(
        @PathVariable(name = "id") id: UUID,
        @Valid @RequestBody request: ServiceTaskHandlerRequest
    ): ResponseEntity<ModifyServiceTaskHandlerResult>

    @DeleteMapping(value = ["/v1/openzaak/link/{id}/service-handler/{processDefinitionKey}/{serviceTaskId}"])
    fun removeServiceTaskHandler(
        @PathVariable(name = "id") id: UUID,
        @PathVariable(name = "processDefinitionKey") processDefinitionKey: String,
        @PathVariable(name = "serviceTaskId") serviceTaskId: String
    ): ResponseEntity<RemoveServiceTaskHandlerResult>
}
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

package com.ritense.connector.web.rest

import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.web.rest.request.CreateConnectorInstanceRequest
import com.ritense.connector.web.rest.request.ModifyConnectorInstanceRequest
import com.ritense.connector.web.rest.result.CreateConnectorInstanceResult
import com.ritense.connector.web.rest.result.ModifyConnectorInstanceResult
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
interface ConnectorResource {

    @GetMapping("/v1/connector/type")
    fun getTypes(): ResponseEntity<List<ConnectorType>>

    @GetMapping("/v1/connector/instance", params = ["instanceId"])
    fun getConnectorInstance(@RequestParam(name = "instanceId") instanceId: UUID): ResponseEntity<ConnectorInstance>

    @GetMapping("/v1/connector/instance")
    fun getInstances(
        @RequestParam(required = false) typeName: String?,
        @PageableDefault(sort = ["name"], direction = Sort.Direction.DESC) pageable: Pageable = Pageable.unpaged()
    ): ResponseEntity<Page<ConnectorInstance>>

    @GetMapping("/v1/connector/instance/{typeId}")
    fun getInstancesByType(
        @PathVariable(name = "typeId") typeId: UUID,
        @PageableDefault(sort = ["name"], direction = Sort.Direction.DESC) pageable: Pageable = Pageable.unpaged()
    ): ResponseEntity<Page<ConnectorInstance>>

    @PostMapping("/v1/connector/instance")
    fun create(
        @Valid @RequestBody request: CreateConnectorInstanceRequest
    ): ResponseEntity<CreateConnectorInstanceResult>

    @PutMapping("/v1/connector/instance")
    fun modify(
        @Valid @RequestBody request: ModifyConnectorInstanceRequest
    ): ResponseEntity<ModifyConnectorInstanceResult>

    @DeleteMapping("/v1/connector/instance/{instanceId}")
    fun remove(@PathVariable(name = "instanceId") instanceId: UUID): ResponseEntity<Void>
}

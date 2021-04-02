/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.openzaak.domain.mapping.impl.InformatieObjectTypeLink
import com.ritense.openzaak.service.result.CreateInformatieObjectTypeLinkResult
import com.ritense.openzaak.web.rest.request.CreateInformatieObjectTypeLinkRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping(value = ["/api/openzaak/informatie-object-type-link"], produces = [MediaType.APPLICATION_JSON_VALUE])
interface InformatieObjectTypeLinkResource {

    @GetMapping(value = ["/{documentDefinitionName}"])
    fun get(@PathVariable(name = "documentDefinitionName") documentDefinitionName: String): ResponseEntity<InformatieObjectTypeLink?>

    @PostMapping
    fun create(@Valid @RequestBody request: CreateInformatieObjectTypeLinkRequest): ResponseEntity<CreateInformatieObjectTypeLinkResult>

    @DeleteMapping(value = ["/{documentDefinitionName}"])
    fun remove(@PathVariable(name = "documentDefinitionName") documentDefinitionName: String): ResponseEntity<InformatieObjectTypeLink?>

}
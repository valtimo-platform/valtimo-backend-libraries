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

package com.ritense.valueresolver.web.rest

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valueresolver.ValueResolverOption
import com.ritense.valueresolver.ValueResolverOptionRequest
import com.ritense.valueresolver.ValueResolverOptionType
import com.ritense.valueresolver.ValueResolverService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class ValueResolverResource(
    private val valueResolverService: ValueResolverService
) {
    @GetMapping("/management/v1/value-resolver")
    fun getValueResolvers(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(valueResolverService.getValueResolvers())
    }

    @PostMapping("/management/v1/value-resolver/document-definition/{documentDefinitionName}/keys")
    fun getResolvableKeys(
        @PathVariable documentDefinitionName: String,
        @RequestBody prefixes: List<String>,
    ): ResponseEntity<List<String>> {
        val options = valueResolverService.getResolvableKeys(
            ValueResolverOptionRequest(prefixes, ValueResolverOptionType.FIELD),
            documentDefinitionName
        )
        return ResponseEntity.ok(options.map { it.path })
    }

    @PostMapping("/management/v2/value-resolver/document-definition/{documentDefinitionName}/keys")
    fun getResolvableKeys(
        @PathVariable documentDefinitionName: String,
        @RequestBody request: ValueResolverOptionRequest
    ): ResponseEntity<List<ValueResolverOption>> {
        return ResponseEntity.ok(valueResolverService.getResolvableKeys(request, documentDefinitionName))
    }

    @PostMapping("/management/v1/value-resolver/document-definition/{documentDefinitionName}/version/{version}/keys")
    fun getResolvableKeys(
        @PathVariable documentDefinitionName: String,
        @PathVariable version: Long,
        @RequestBody prefixes: List<String>,
    ): ResponseEntity<List<String>> {
            val options = valueResolverService.getResolvableKeys(
                ValueResolverOptionRequest(prefixes, ValueResolverOptionType.FIELD),
                documentDefinitionName,
                version
            )
        return ResponseEntity.ok(options.map { it.path })
    }

    @PostMapping("/management/v2/value-resolver/document-definition/{documentDefinitionName}/version/{version}/keys")
    fun getResolvableKeys(
        @PathVariable documentDefinitionName: String,
        @PathVariable version: Long,
        @RequestBody request: ValueResolverOptionRequest
    ): ResponseEntity<List<ValueResolverOption>> {
        return ResponseEntity.ok(valueResolverService.getResolvableKeys(request, documentDefinitionName, version))
    }
}
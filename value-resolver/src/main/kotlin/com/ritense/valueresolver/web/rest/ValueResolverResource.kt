package com.ritense.valueresolver.web.rest

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valueresolver.ValueResolverService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class ValueResolverResource(
    private val valueResolverService: ValueResolverService
) {
    @GetMapping("/v1/value-resolver")
    fun getValueResolvers(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(valueResolverService.getValueResolvers())
    }

    @GetMapping("/v1/value-resolver/prefix/{prefix}/document-definition/{documentDefinitionName}/keys")
    fun getResolvableKeys(
        @PathVariable prefix: String,
        @PathVariable documentDefinitionName: String
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(valueResolverService.getResolvableKeys(prefix, documentDefinitionName))
    }

    @GetMapping("/v1/value-resolver/prefix/{prefix}/document-definition/{documentDefinitionName}/version/{version}/keys")
    fun getResolvableKeys(
        @PathVariable prefix: String,
        @PathVariable documentDefinitionName: String,
        @PathVariable version: Long
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(valueResolverService.getResolvableKeys(prefix, documentDefinitionName, version))
    }
}
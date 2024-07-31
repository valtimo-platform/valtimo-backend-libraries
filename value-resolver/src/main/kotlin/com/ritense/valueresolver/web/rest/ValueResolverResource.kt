package com.ritense.valueresolver.web.rest

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
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
    @GetMapping("/v1/value-resolver")
    fun getValueResolvers(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(valueResolverService.getValueResolvers())
    }

    @PostMapping("/v1/value-resolver/document-definition/{documentDefinitionName}/keys")
    fun getResolvableKeys(
        @PathVariable documentDefinitionName: String,
        @RequestBody prefixes: List<String>,
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(valueResolverService.getResolvableKeys(prefixes, documentDefinitionName))
    }

    @PostMapping("/v1/value-resolver/document-definition/{documentDefinitionName}/version/{version}/keys")
    fun getResolvableKeys(
        @PathVariable documentDefinitionName: String,
        @PathVariable version: Long,
        @RequestBody prefixes: List<String>,
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(valueResolverService.getResolvableKeys(prefixes, documentDefinitionName, version))
    }
}
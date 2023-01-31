package com.ritense.search.web.rest

import com.ritense.search.domain.SearchField
import com.ritense.search.service.SearchFieldService
import javax.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/v1/search/field", produces = [MediaType.APPLICATION_JSON_VALUE])
class SearchFieldResource(
    private val searchFieldService: SearchFieldService
) {

    @PostMapping("/{ownerId}")
    fun create(
        @PathVariable ownerId: String,
        @Valid @RequestBody searchField: SearchField
    ) =
        ResponseEntity.ok(searchFieldService.create(searchField))

    @PutMapping("/{ownerId}/{key}")
    fun update(
        @PathVariable ownerId: String,
        @PathVariable key: String,
        @Valid @RequestBody searchField: SearchField
    ) =
        ResponseEntity.ok(searchFieldService.update(ownerId, key, searchField))

    @GetMapping("/{ownerId}")
    fun getByKey(@PathVariable ownerId: String) =
        ResponseEntity.ok(searchFieldService.findByOwnerId(ownerId))

    @DeleteMapping("/{ownerId}/{key}")
    fun delete(
        @PathVariable ownerId: String,
        @PathVariable key: String
    ): ResponseEntity<Any> {
        searchFieldService.delete(ownerId, key)
        return ResponseEntity.noContent().build()
    }
}
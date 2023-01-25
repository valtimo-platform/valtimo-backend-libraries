package com.ritense.search.web.rest

import com.ritense.search.domain.SearchListColumn
import com.ritense.search.service.SearchListColumnService
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
class SearchListColumnResource(
    private val searchListColumnService: SearchListColumnService
) {

    @PostMapping
    fun create(@Valid @RequestBody searchListColumn: SearchListColumn) =
        ResponseEntity.ok(searchListColumnService.create(searchListColumn))

    @PutMapping
    fun update(@Valid @RequestBody searchListColumn: SearchListColumn) =
    ResponseEntity.ok(searchListColumnService.update(searchListColumn))

    @GetMapping("/{key}")
    fun getByKey(@PathVariable key: String) =
        ResponseEntity.ok(searchListColumnService.findByKey(key))

    @GetMapping
    fun getAll() = ResponseEntity.ok(searchListColumnService.getAll())

    @DeleteMapping
    fun delete(@Valid @RequestBody searchListColumn: SearchListColumn): ResponseEntity<Any> {
        searchListColumnService.delete(searchListColumn)
        return ResponseEntity.noContent().build()
    }
}
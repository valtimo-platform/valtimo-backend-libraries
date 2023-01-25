package com.ritense.search.service

import com.ritense.search.domain.SearchListColumn
import com.ritense.search.repository.SearchListColumnRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class SearchListColumnService(
    private val searchListColumnRepository: SearchListColumnRepository
) {

    fun create(searchListColumn: SearchListColumn) = searchListColumnRepository.save(searchListColumn)

    fun update(searchListColumn: SearchListColumn) =
        with(findByKey(searchListColumn.key)) {
            if (this != null) {
                if (searchListColumn.key != key) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This key already exists. Please choose another key"
                    )
                }
            }
            searchListColumnRepository.save(searchListColumn)
        }

    fun findByKey(key: String) = searchListColumnRepository.findByIdOrNull(key)

    fun getAll() = searchListColumnRepository.findAll()

    fun delete(searchListColumn: SearchListColumn) = searchListColumnRepository.delete(searchListColumn)
}
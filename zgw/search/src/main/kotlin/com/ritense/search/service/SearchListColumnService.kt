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
        with(findByOwnerId(searchListColumn.ownerId)) {
            if (this != null) {
                if (searchListColumn.ownerId != ownerId) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This ownerId already exists. Please choose another ownerId"
                    )
                }
            }
            searchListColumnRepository.save(searchListColumn)
        }

    fun findByOwnerId(ownerId: String) = searchListColumnRepository.findByIdOrNull(ownerId)

    fun getAll() = searchListColumnRepository.findAll()

    fun delete(searchListColumn: SearchListColumn) = searchListColumnRepository.delete(searchListColumn)
}
package com.ritense.search.repository

import com.ritense.search.domain.SearchListColumn
import org.springframework.data.jpa.repository.JpaRepository

interface SearchListColumnRepository: JpaRepository<SearchListColumn, String> {
}
package com.ritense.objectmanagement.domain.search

data class SearchWithConfigFilter(
    val key: String,
    val rangeFrom: SearchRequestValue?,
    val rangeTo: SearchRequestValue?,
    val values: List<SearchRequestValue> = listOf()
)

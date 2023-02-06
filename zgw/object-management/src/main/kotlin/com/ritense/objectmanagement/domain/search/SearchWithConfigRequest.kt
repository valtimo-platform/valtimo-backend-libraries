package com.ritense.objectmanagement.domain.search

class SearchWithConfigRequest(
    val searchOperator: SearchOperator = SearchOperator.AND,
    val assigneeFilter: AssigneeFilter = AssigneeFilter.ALL,
    val otherFilters: List<SearchWithConfigFilter> = listOf()
) {
}
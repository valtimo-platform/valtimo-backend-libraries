package com.ritense.search.domain


enum class SearchFieldMatchType(val simpleName: String) {
    LIKE("like"),
    EXACT("exact");

    override fun toString(): String {
        return simpleName
    }
}
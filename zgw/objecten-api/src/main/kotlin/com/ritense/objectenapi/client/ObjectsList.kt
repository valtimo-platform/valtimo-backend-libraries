package com.ritense.objectenapi.client

class ObjectsList(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<ObjectWrapper>
) {
}
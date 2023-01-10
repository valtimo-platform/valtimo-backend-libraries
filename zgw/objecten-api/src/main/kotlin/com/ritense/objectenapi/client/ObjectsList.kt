package com.ritense.objectenapi.client

class ObjectsList(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<ObjectRecord>
) {
}
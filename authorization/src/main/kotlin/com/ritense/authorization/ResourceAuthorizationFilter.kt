package com.ritense.authorization

class ResourceAuthorizationFilter(
    val resourceKey: String, // e.g. document-definition
    val values: List<String>, // e.g. leningen
    val action: Action
)
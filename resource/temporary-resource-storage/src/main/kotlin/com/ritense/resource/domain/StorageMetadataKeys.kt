package com.ritense.resource.domain

enum class StorageMetadataKeys(val key: String) {
    DOCUMENT_ID("documentId"),
    DOWNLOAD_URL("downloadUrl"),
}

fun getEnumFromKey(key: String): StorageMetadataKeys {
    return StorageMetadataKeys.entries.find { it.key == key }!!
}
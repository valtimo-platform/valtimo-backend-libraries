package com.ritense.resource.domain

enum class StorageMetadataKeys(val key: String) {
    DOCUMENT_ID("documentId"),
    DOWNLOAD_URL("downloadUrl"),
}

fun getEnumFromKey(key: String): Result<StorageMetadataKeys> {
    return StorageMetadataKeys.entries.find { it.key == key }
        ?.let { Result.success(it) }
        ?: Result.failure(IllegalArgumentException("Unknown storage metadata key: $key"))
}
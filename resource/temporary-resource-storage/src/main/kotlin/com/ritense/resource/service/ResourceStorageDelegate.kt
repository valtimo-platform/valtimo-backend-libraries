package com.ritense.resource.service

class ResourceStorageDelegate(
    private val service: TemporaryResourceStorageService
) {

    fun getMetadata(resourceStorageFileId: String, metadataKey: String): String {
        return service.getMetadataValue(resourceStorageFileId, metadataKey)
    }
}
package com.ritense.resource.service

import com.ritense.resource.domain.ResourceStorageMetadataId
import com.ritense.resource.domain.getEnumFromKey
import com.ritense.resource.repository.ResourceStorageMetadataRepository

class ResourceStorageDelegate(
    private val repository: ResourceStorageMetadataRepository
) {

    fun getMetadata(resourceStorageFileId: String, metadataKey: String): String {
        return repository.getReferenceById(ResourceStorageMetadataId(
            fileId = resourceStorageFileId,
            metadataKey = getEnumFromKey(metadataKey)
        )).metadataValue
    }
}
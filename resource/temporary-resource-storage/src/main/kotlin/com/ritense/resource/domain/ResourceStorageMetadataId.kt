package com.ritense.resource.domain

import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ResourceStorageMetadataId(
    @Column(name="resource_storage_file_id")
    var fileId: String,
    @Column(name="metadata_key")
    var metadataKey: StorageMetadataKeys,
) : AbstractId<ResourceStorageMetadataId>()
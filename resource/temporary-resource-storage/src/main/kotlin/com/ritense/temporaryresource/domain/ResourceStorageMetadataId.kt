package com.ritense.temporaryresource.domain

import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class ResourceStorageMetadataId(
    @Column(name="resource_storage_file_id", nullable = false, columnDefinition = "varchar(50)")
    var fileId: String,
    @Column(name="metadata_key", nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    var metadataKey: StorageMetadataKeys,
) : AbstractId<ResourceStorageMetadataId>()
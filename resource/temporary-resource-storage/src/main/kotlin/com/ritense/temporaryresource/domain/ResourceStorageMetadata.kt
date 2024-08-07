package com.ritense.temporaryresource.domain

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "resource_storage_metadata")
class ResourceStorageMetadata(
    @EmbeddedId
    var id: ResourceStorageMetadataId,
    @Column(name="metadata_value")
    var metadataValue: String
)
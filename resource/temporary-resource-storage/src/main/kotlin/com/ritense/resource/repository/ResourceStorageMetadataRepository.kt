package com.ritense.resource.repository

import com.ritense.resource.domain.ResourceStorageMetadata
import com.ritense.resource.domain.ResourceStorageMetadataId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourceStorageMetadataRepository: JpaRepository<ResourceStorageMetadata, ResourceStorageMetadataId> {}
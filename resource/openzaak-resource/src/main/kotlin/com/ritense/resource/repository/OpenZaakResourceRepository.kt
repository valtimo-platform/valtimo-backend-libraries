package com.ritense.resource.repository

import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.domain.ResourceId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface OpenZaakResourceRepository : JpaRepository<OpenZaakResource, ResourceId> {

    override fun findById(id: ResourceId): Optional<OpenZaakResource>

}
/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.resource.repository

import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.domain.ResourceId
import org.springframework.data.jpa.repository.JpaRepository
import java.net.URI
import java.util.Optional

interface OpenZaakResourceRepository : JpaRepository<OpenZaakResource, ResourceId> {

    override fun findById(id: ResourceId): Optional<OpenZaakResource>
    fun findByInformatieObjectUrl(url: URI): OpenZaakResource

}
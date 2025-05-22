/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.search.repository

import com.ritense.search.domain.SearchFieldV2
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SearchFieldV2Repository : JpaRepository<SearchFieldV2, UUID> {

    fun findByOwnerIdAndKeyOrderByOrder(ownerId: String, key: String): SearchFieldV2?

    fun findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(ownerType: String, ownerId: String, key: String): SearchFieldV2?

    fun findAllByOwnerTypeOrderByOrder(ownerType: String?): List<SearchFieldV2>

    fun findAllByOwnerIdOrderByOrder(ownerId: String): List<SearchFieldV2>

    fun findAllByOwnerTypeAndOwnerIdOrderByOrder(ownerType: String?, ownerId: String): List<SearchFieldV2>

    fun deleteAllByOwnerType(ownerType: String)
}
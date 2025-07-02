/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.tab.service

import com.ritense.tab.domain.Tab
import com.ritense.tab.repository.TabRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@SkipComponentScan
@Service
class TabService(
    private val tabRepository: TabRepository,
) {

    fun create(tab: Tab): Tab {
        require(findById(tab.id) == null)
        require(findByOwnerAndKey(tab.ownerType, tab.ownerId, tab.key) == null)
        return tabRepository.save(tab)
    }

    fun update(tab: Tab): Tab {
        val existingTab = findById(tab.id)
            ?: findByOwnerAndKey(tab.ownerType, tab.ownerId, tab.key)
            ?: throw IllegalStateException("Search list tab not found")
        return tabRepository.save(tab.copy(id = existingTab.id))
    }

    fun findAllByOwner(ownerType: String, ownerId: String) =
        tabRepository.findAllByOwnerTypeAndOwnerIdOrderByOrder(ownerType, ownerId)

    fun findById(id: UUID): Tab? =
        tabRepository.findById(id).getOrNull()

    fun findByOwnerAndKey(ownerType: String, ownerId: String, key: String): Tab? =
        tabRepository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(ownerType, ownerId, key)

    fun deleteAllByOwner(ownerType: String, ownerId: String) =
        tabRepository.deleteAllByOwnerTypeAndOwnerId(ownerType, ownerId)

    fun delete(ownerType: String, ownerId: String, key: String) =
        tabRepository.deleteAllByOwnerTypeAndOwnerIdAndKey(ownerType, ownerId, key)

    fun updateList(tab: List<Tab>) {
        tabRepository.saveAll(
            tab.mapIndexed { index, tab ->
                tab.copy(order = index)
            }
        )
    }
}
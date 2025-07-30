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

package com.ritense.iko.service

import com.ritense.iko.domain.IkoDataAggregateTab
import com.ritense.iko.domain.IkoDataAggregateTabId
import com.ritense.iko.repository.IkoDataAggregateTabRepository
import com.ritense.tab.domain.Tab
import com.ritense.tab.service.TabService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@SkipComponentScan
@Service
class IkoTabService(
    private val tabService: TabService,
    private val ikoDataAggregateTabRepository: IkoDataAggregateTabRepository,
) {

    fun findByKey(ikoDataAggregateKey: String, tabKey: String): Tab? {
        return ikoDataAggregateTabRepository.findByIdIkoDataAggregateKeyAndTabKey(ikoDataAggregateKey, tabKey)?.tab
    }

    fun getByKey(ikoDataAggregateKey: String, tabKey: String): Tab {
        return findByKey(ikoDataAggregateKey, tabKey)
            ?: error("Unknown tab key: $tabKey")
    }

    fun findAllTabsByIkoDataAggregateKey(ikoDataAggregateKey: String): List<Tab> {
        return ikoDataAggregateTabRepository.findAllByIdIkoDataAggregateKeyOrderByTabOrder(ikoDataAggregateKey)
            .map { it.tab }
    }

    fun deleteByKey(ikoDataAggregateKey: String, tabKey: String) {
        ikoDataAggregateTabRepository.deleteByIdIkoDataAggregateKeyAndTabKey(ikoDataAggregateKey, tabKey)
        ikoDataAggregateTabRepository.flush()
    }

    fun create(ikoDataAggregateKey: String, tab: Tab): Tab {
        val tabs = findAllTabsByIkoDataAggregateKey(ikoDataAggregateKey)
        require(tabs.none { it.key == tab.key })
        val createdTab = tabService.create(tab.copy(order = tabs.maxOf { it.order + 1 }))
        ikoDataAggregateTabRepository.save(
            IkoDataAggregateTab(
                id = IkoDataAggregateTabId(ikoDataAggregateKey, tab.id),
                tab = createdTab,
            )
        )
        return createdTab
    }

    fun update(ikoDataAggregateKey: String, tab: Tab): Tab {
        val ikoDataAggregateTab = ikoDataAggregateTabRepository.findByIdIkoDataAggregateKeyAndTabKey(
            ikoDataAggregateKey,
            tab.key
        )
        requireNotNull(ikoDataAggregateTab)
        val updatedTab = tabService.update(tab.copy(id = ikoDataAggregateTab.id.tabId))
        ikoDataAggregateTabRepository.save(
            IkoDataAggregateTab(
                id = IkoDataAggregateTabId(ikoDataAggregateKey, updatedTab.id),
                tab = updatedTab,
            )
        )
        return updatedTab
    }

}
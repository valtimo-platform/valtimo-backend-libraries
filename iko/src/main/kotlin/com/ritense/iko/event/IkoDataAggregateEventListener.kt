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

package com.ritense.iko.event

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.service.IkoListColumnService
import com.ritense.iko.service.IkoTabService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class IkoDataAggregateEventListener(
    private val ikoDataRequestService: IkoDataRequestService,
    private val ikoListColumnService: IkoListColumnService,
    private val ikoTabService: IkoTabService,
) {

    @RunWithoutAuthorization
    @EventListener(IkoDataAggregatePreDeleteEvent::class)
    fun deleteIkoDataRequests(event: IkoDataAggregatePreDeleteEvent) {
        ikoDataRequestService.findAll(
            ikoDataAggregateKey = event.ikoDataAggregateKey,
        ).forEach { ikoDataRequest ->
            ikoDataRequestService.delete(
                ikoDataAggregateKey = event.ikoDataAggregateKey,
                key = ikoDataRequest.id.key,
            )
        }
    }

    @RunWithoutAuthorization
    @EventListener(IkoDataAggregatePreDeleteEvent::class)
    fun deleteIkoListColumns(event: IkoDataAggregatePreDeleteEvent) {
        ikoListColumnService.findAllColumnsByIkoDataAggregateKey(
            ikoDataAggregateKey = event.ikoDataAggregateKey,
        ).forEach { listColumn ->
            ikoListColumnService.deleteByKey(
                ikoDataAggregateKey = event.ikoDataAggregateKey,
                columnKey = listColumn.key
            )
        }
    }

    @RunWithoutAuthorization
    @EventListener(IkoDataAggregatePreDeleteEvent::class)
    fun deleteIkoTabs(event: IkoDataAggregatePreDeleteEvent) {
        ikoTabService.findAllTabsByIkoDataAggregateKey(
            ikoDataAggregateKey = event.ikoDataAggregateKey,
        ).forEach { tab ->
            ikoTabService.deleteByKey(
                ikoDataAggregateKey = event.ikoDataAggregateKey,
                tabKey = tab.key
            )
        }
    }
}
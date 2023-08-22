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

package com.ritense.valtimo.contract.domain

abstract class AbstractAggregateRoot {

    @org.springframework.data.annotation.Transient
    @Transient
    private var domainEvents: List<DomainEvent> = emptyList()

    fun registerEvent(event: DomainEvent) {
        domainEvents = domainEvents + event
    }

    fun domainEvents() = domainEvents

    fun clearDomainEvents() {
        if (domainEvents.isNotEmpty()) {
            domainEvents = emptyList()
        }
    }

    fun andEventsFrom(aggregate: AbstractAggregateRoot): AbstractAggregateRoot {
        aggregate.domainEvents().map { registerEvent(it) }
        aggregate.clearDomainEvents()
        return this
    }

}
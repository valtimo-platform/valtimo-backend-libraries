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

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import java.util.Collections

/**
 * Aggregate Root base class based on the Spring Data one {@link org.springframework.data.domain.AbstractAggregateRoot}.
 */
abstract class AggregateRoot<EventType> {

    @Transient
    private val domainEvents = ArrayList<EventType>()

    /**
     * All domain events currently captured by the aggregate.
     */
    @JsonIgnore
    @DomainEvents
    fun domainEvents(): Collection<EventType> {
        return Collections.unmodifiableList(domainEvents)
    }

    /**
     * Registers the given event object for publication on a call to a Spring Data repository's save methods.
     */
    protected fun registerEvent(event: EventType): EventType {
        this.domainEvents.add(event)
        return event
    }

    /**
     * Clears all domain events currently held. Usually invoked by the infrastructure in place in Spring Data
     * repositories.
     */
    @AfterDomainEventPublication
    fun clearDomainEvents() {
        this.domainEvents.clear()
    }

}
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

package com.ritense.document.util.hibernate

import com.ritense.valtimo.contract.domain.AbstractAggregateRoot
import mu.KotlinLogging
import org.hibernate.event.spi.FlushEntityEvent
import org.hibernate.event.spi.FlushEntityEventListener
import org.springframework.context.ApplicationEventPublisher

class PersistedEventListener(
    private val applicationEventPublisher: ApplicationEventPublisher
) : FlushEntityEventListener {

    override fun onFlushEntity(event: FlushEntityEvent) {
        event.entity.let { entity ->
            if (entity is AbstractAggregateRoot) {
                logger.debug {
                    "onFlushEntity: Processing aggregate root (${entity.javaClass.simpleName}) " +
                        "events (count=${entity.domainEvents().size}) "
                }
                // Hibernate also flushes for queries, so we need to prevent double publishing.
                val events = entity.domainEvents().toList()
                entity.clearDomainEvents()
                events.forEach { domainEvent ->
                    applicationEventPublisher.publishEvent(domainEvent)
                }.also {
                    logger.debug {
                        "Published events (count=${entity.domainEvents().size}) "
                    }
                }
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}
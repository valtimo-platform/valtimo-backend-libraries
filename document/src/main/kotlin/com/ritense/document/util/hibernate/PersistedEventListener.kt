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
                entity.domainEvents().forEach { domainEvent ->
                    applicationEventPublisher.publishEvent(domainEvent)
                }.also {
                    logger.debug {
                        "Published events (count=${entity.domainEvents().size}) "
                    }
                    entity.clearDomainEvents()
                }
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}
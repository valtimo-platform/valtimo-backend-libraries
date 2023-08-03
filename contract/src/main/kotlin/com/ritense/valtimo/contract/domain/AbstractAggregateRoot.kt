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
package com.ritense.document.util.hibernate

import org.hibernate.boot.Metadata
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.EventType
import org.hibernate.integrator.spi.Integrator
import org.hibernate.service.spi.SessionFactoryServiceRegistry

class HibernateEventListenerIntegrator(
    private val persistedEventListener: PersistedEventListener
) : Integrator {

    override fun integrate(
        metadata: Metadata,
        sessionFactory: SessionFactoryImplementor,
        serviceRegistry: SessionFactoryServiceRegistry
    ) {
        val eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry::class.java)
        eventListenerRegistry.appendListeners(EventType.FLUSH_ENTITY, persistedEventListener)
    }

    override fun disintegrate(
        sessionFactory: SessionFactoryImplementor,
        serviceRegistry: SessionFactoryServiceRegistry
    ) {
    }

}
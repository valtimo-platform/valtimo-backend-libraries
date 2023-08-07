package com.ritense.document.autoconfigure

import com.ritense.document.util.hibernate.HibernateConfig
import com.ritense.document.util.hibernate.HibernateEventListenerIntegrator
import com.ritense.document.util.hibernate.PersistedEventListener
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HibernateListenerAutoConfiguration {

    @Bean
    fun hibernateConfig(
        hibernateEventListenerIntegrator: HibernateEventListenerIntegrator
    ) = HibernateConfig(hibernateEventListenerIntegrator)

    @Bean
    fun persistedEventListener(
        applicationEventPublisher: ApplicationEventPublisher
    ) = PersistedEventListener(applicationEventPublisher)

    @Bean
    fun hibernateEventListenerIntegrator(
        persistedEventListener: PersistedEventListener
    ) = HibernateEventListenerIntegrator(persistedEventListener)

}
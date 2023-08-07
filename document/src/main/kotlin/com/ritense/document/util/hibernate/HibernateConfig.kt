package com.ritense.document.util.hibernate

import org.hibernate.integrator.spi.Integrator
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer

class HibernateConfig(
    private val integrator: HibernateEventListenerIntegrator
) : HibernatePropertiesCustomizer {

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        // Assuming you have a mutable map called hibernateProperties and a variable called integrator
        val integratorProviderKey = "hibernate.integrator_provider"

        // Check if the key already exists in the map
        if (hibernateProperties.containsKey(integratorProviderKey)) {
            // If the key exists, retrieve the current list and append the new integrator to it
            val currentIntegratorList = hibernateProperties[integratorProviderKey] as MutableList<Integrator>
            hibernateProperties[integratorProviderKey] = currentIntegratorList + integrator
        } else {
            // If the key doesn't exist, create a new list containing the integrator and assign it to the key
            hibernateProperties[integratorProviderKey] = mutableListOf(integrator)
        }
    }

}
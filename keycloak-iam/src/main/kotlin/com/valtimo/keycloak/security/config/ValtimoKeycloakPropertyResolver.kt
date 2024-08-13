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

package com.valtimo.keycloak.security.config

import com.valtimo.keycloak.service.KeycloakService.KEYCLOAK_API_CLIENT_REGISTRATION
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties
import org.springframework.context.ApplicationContext
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

class ValtimoKeycloakPropertyResolver(
    properties: KeycloakSpringBootProperties,
    applicationContext: ApplicationContext
) {

    init {
        initProperties = properties
        clientRegistrationRepository = applicationContext.getBeansOfType(ClientRegistrationRepository::class.java)
            .values.firstOrNull()
    }

    companion object {
        private var initProperties: KeycloakSpringBootProperties? = null
        private var clientRegistrationRepository: ClientRegistrationRepository? = null
        private var properties: KeycloakSpringBootProperties? = null

        @JvmStatic
        fun resolveProperties(): KeycloakSpringBootProperties {
            if (clientRegistrationRepository == null) {
                properties = initProperties
                return properties!!
            }
            val clientRegistration =
                clientRegistrationRepository!!.findByRegistrationId(KEYCLOAK_API_CLIENT_REGISTRATION)

            if (clientRegistration == null) {
                properties = initProperties
                return properties!!
            }

            val parts = clientRegistration.providerDetails.issuerUri.split("realms/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val authServerUrl = parts[0]
            val realm = parts[1].replace("/", "")
            properties = KeycloakSpringBootProperties(
                realm,
                authServerUrl,
                clientRegistration.clientId,
                mapOf("secret" to clientRegistration.clientSecret)
            )

            return properties!!
        }
    }

}
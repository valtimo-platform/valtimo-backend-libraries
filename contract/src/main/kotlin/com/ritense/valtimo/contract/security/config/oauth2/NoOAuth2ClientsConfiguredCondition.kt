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
package com.ritense.valtimo.contract.security.config.oauth2

import org.springframework.boot.autoconfigure.condition.ConditionMessage
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotatedTypeMetadata

// The opposite of: org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition
class NoOAuth2ClientsConfiguredCondition : SpringBootCondition() {
    override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val message = ConditionMessage.forCondition("No OAuth2 Clients Configured Condition")
        val registrations = getRegistrations(context.environment)
        return if (registrations.isNotEmpty()) {
            ConditionOutcome.noMatch(
                message.foundExactly(
                    "registered oauth2 clients ${registrations.values.joinToString { it.clientId ?: "NULL" }}"
                )
            )
        } else {
            ConditionOutcome.match(message.notAvailable("registered oauth2 clients"))
        }
    }

    private fun getRegistrations(environment: Environment): Map<String, OAuth2ClientProperties.Registration> {
        return Binder.get(environment)
            .bind("spring.security.oauth2.client.registration", STRING_REGISTRATION_MAP)
            .orElse(emptyMap())
    }

    companion object {
        private val STRING_REGISTRATION_MAP: Bindable<Map<String, OAuth2ClientProperties.Registration>> =
            Bindable.mapOf(String::class.java, OAuth2ClientProperties.Registration::class.java)
    }
}
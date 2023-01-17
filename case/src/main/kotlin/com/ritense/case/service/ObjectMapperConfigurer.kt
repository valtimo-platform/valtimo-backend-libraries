/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.case.domain.DisplayTypeParameter
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener

class ObjectMapperConfigurer(
    private val objectMapper: ObjectMapper,
    private val displayTypeParameterTypes: Collection<NamedType>
) {

    @EventListener(ApplicationStartedEvent::class)
    fun configure() {
        logger.debug { "Setup ObjectMapper for DisplayTypeParameters" }
        objectMapper
            .apply {
                displayTypeParameterTypes
                    .filter { DisplayTypeParameter::class.java.isAssignableFrom(it.type) }
                    .forEach {
                        registerSubtypes(it)
                    }
            }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}

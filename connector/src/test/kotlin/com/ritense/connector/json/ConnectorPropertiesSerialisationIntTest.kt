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

package com.ritense.connector.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.connector.BaseIntegrationTest
import com.ritense.connector.config.SpringHandlerInstantiatorImpl
import com.ritense.connector.impl.ObjectApiProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class ConnectorPropertiesSerialisationIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var springHandlerInstantiatorImpl: SpringHandlerInstantiatorImpl

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `round trip test`() {
        objectMapper.setHandlerInstantiator(springHandlerInstantiatorImpl)

        val rawJson = "{" +
            " \"className\":\"com.ritense.connector.impl.ObjectApiProperties\", \n" +
            " \"nestedObject\":{\"name\":\"aSecretValue\"} \n" +
            "}"

        val connectorProperties: ObjectApiProperties = objectMapper.readValue(rawJson)

        assertThat(connectorProperties).isNotNull

        connectorProperties.nestedObject.name
        connectorProperties.nestedObject.name = "aNew"

        assertThat(connectorProperties).isNotNull

        val encrypted = objectMapper.writeValueAsString(connectorProperties)

        assertThat(encrypted).isNotNull

        val objectApiProperties: ObjectApiProperties = objectMapper.readValue(encrypted)

        assertThat(objectApiProperties).isNotNull
    }
}
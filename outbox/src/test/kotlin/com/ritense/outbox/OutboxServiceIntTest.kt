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

package com.ritense.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.outbox.domain.BaseEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class OutboxServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxService: OutboxService

    @Autowired
    lateinit var outboxMessageRepository: OutboxMessageRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @Transactional
    fun `should create OutboxMessage`() {
        val event = OrderCreatedEvent("textBook")

        outboxService.send(objectMapper.valueToTree<ObjectNode>(event))

        val messages = outboxMessageRepository.findAll()
        assertThat(messages.size).isEqualTo(1)
        assertThat(objectMapper.writeValueAsString(messages[0].message)).isEqualTo("""{"name":"textBook"}""")
    }

    @Test
    fun `should throw error when no transaction exists`() {
        val event = OrderCreatedEvent("textBook")

        val exception = assertThrows<RuntimeException> {
            outboxService.send(objectMapper.valueToTree<ObjectNode>(event))
        }

        assertThat(exception.message).isEqualTo("No existing transaction found for transaction marked with propagation 'mandatory'")
    }

    @Test
    @Transactional
    fun `should set source to 'application' when no application name or system user id is provided`() {
        outboxService.send(TestEvent())

        val messages = outboxMessageRepository.findAll()
        assertThat(messages.size).isEqualTo(1)
        assertThat(messages[0].message.get("source").textValue()).isEqualTo("application")
    }

    data class OrderCreatedEvent(
        val name: String
    )

    class TestEvent: BaseEvent(
        specversion = "1.0",
        type = "test",
        resultType = "test",
        resultId = "test",
        result = jacksonObjectMapper().createObjectNode(),
        source = ""
    )
}

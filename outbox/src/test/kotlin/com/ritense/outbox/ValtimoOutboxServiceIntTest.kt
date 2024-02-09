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

package com.ritense.outbox

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.outbox.domain.BaseEvent
import com.ritense.outbox.exception.OutboxTransactionReadOnlyException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.annotation.Transactional

class ValtimoOutboxServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxService: ValtimoOutboxService

    @Test
    @Transactional
    fun `should create OutboxMessage`() {
        val event = OrderCreatedEvent("textBook")

        outboxService.send(objectMapper.writeValueAsString(event))


        val messages = outboxMessageRepository.findAll()
        assertThat(messages.size).isEqualTo(1)
        assertThat(messages[0].message).isEqualTo("""{"name":"textBook"}""")
    }

    @Test
    @Transactional(readOnly = true)
    fun `should throw error when read-only transaction`() {
        val event = OrderCreatedEvent("textBook")

        assertThrows<OutboxTransactionReadOnlyException> {
            outboxService.send(objectMapper.writeValueAsString(event))
        }
    }

    @Test
    fun `should throw error when no transaction exists`() {
        val event = OrderCreatedEvent("textBook")

        val exception = assertThrows<IllegalTransactionStateException> {
            outboxService.send(objectMapper.writeValueAsString(event))
        }

        assertThat(exception.message).isEqualTo("No existing transaction found for transaction marked with propagation 'mandatory'")
    }

    @Test
    @Transactional
    fun `should save a cloud event if a base event is submitted`() {
        outboxService.send { TestEvent() }

        val messages = outboxMessageRepository.findAll()
        assertThat(messages.size).isEqualTo(1)
        val result: ObjectNode = objectMapper.readValue(messages[0].message)
        assertThat(result["specversion"].textValue()).isEqualTo("1.0")
    }

    @Test
    @Transactional
    fun `should set source to 'application' when no application name or system user id is provided`() {
        outboxService.send { TestEvent() }

        val messages = outboxMessageRepository.findAll()
        assertThat(messages.size).isEqualTo(1)
        val result: ObjectNode = objectMapper.readValue(messages[0].message)
        assertThat(result["source"].textValue()).isEqualTo("application")
    }

    @Test
    @Transactional
    fun `should set user id to 'System' if no user is available`() {
        outboxService.send { TestEvent() }

        val messages = outboxMessageRepository.findAll()
        assertThat(messages.size).isEqualTo(1)
        val result: ObjectNode = objectMapper.readValue(messages[0].message)
        assertThat(result["data"]["userId"].textValue()).isEqualTo("System")
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ["ADMIN", "USER"])
    @Transactional
    fun `should correctly set user id and roles for a cloud event`() {
        outboxService.send { TestEvent() }

        val messages = outboxMessageRepository.findAll()
        assertThat(messages.size).isEqualTo(1)
        val result: ObjectNode = objectMapper.readValue(messages[0].message)
        assertThat(result["data"]["userId"].textValue()).isEqualTo("user@ritense.com")
        val roles = result["data"]["roles"].toList().map { it.textValue() }
        assertThat(roles).containsExactlyInAnyOrder("ADMIN", "USER")
    }

    data class OrderCreatedEvent(
        val name: String
    )

    class TestEvent : BaseEvent(
        type = "test",
        resultType = "test",
        resultId = "test",
        result = jacksonObjectMapper().createObjectNode()
    )
}

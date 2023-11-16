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

package com.ritense.idempotency.service

import com.ritense.idempotency.BaseIntegrationTest
import com.ritense.idempotency.domain.IdempotencyMessage
import java.time.LocalDateTime
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional

@Transactional
class IdempotencyMessageServiceIntTest : BaseIntegrationTest() {

    @Test
    fun `should store IdempotencyEvent`() {
        val idempotencyMessage = IdempotencyMessage(
            id = UUID.randomUUID(),
            consumer = "test-consumer.fifo",
            messageId = "hfksahdksbadk",
            processedOn = LocalDateTime.now()
        )

        val savedEvent = idempotencyMessageService.store(idempotencyMessage)

        assertThat(savedEvent).isNotNull
    }

    @Test
    fun `should not double store IdempotencyEvent`() {
        val idempotencyMessage = IdempotencyMessage(
            id = UUID.randomUUID(),
            consumer = "test-consumer.fifo",
            messageId = "hfksahdksbadk",
            processedOn = LocalDateTime.now()
        )

        val idempotencyMessage2 = IdempotencyMessage(
            id = UUID.randomUUID(),
            consumer = "test-consumer.fifo",
            messageId = "hfksahdksbadk",
            processedOn = LocalDateTime.now()
        )

        idempotencyMessageService.store(idempotencyMessage)

        assertThrows<DataIntegrityViolationException> {
            idempotencyMessageService.store(idempotencyMessage2)
        }
    }

    @Test
    fun `should check IdempotencyEvent`() {
        val idempotencyMessage = IdempotencyMessage(
            id = UUID.randomUUID(),
            consumer = "test-consumer.fifo",
            messageId = "hfksahdksbadk",
            processedOn = LocalDateTime.now()
        )

        val savedEvent = idempotencyMessageService.store(idempotencyMessage)

        val check = idempotencyMessageService.isProcessed(savedEvent.consumer, savedEvent.messageId)

        assertThat(check).isTrue()
    }
}
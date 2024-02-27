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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.publisher.MessagePublisher
import com.ritense.outbox.repository.OutboxMessageRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Tag("integration")
class BaseIntegrationTest {

    @MockBean
    lateinit var messagePublisher: MessagePublisher

    @SpyBean
    lateinit var outboxMessageRepository: OutboxMessageRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @AfterEach
    fun afterEach() {
        outboxMessageRepository.deleteAll()
    }

    fun insertOutboxMessage(event: Any) {
        val message = OutboxMessage(
            message = objectMapper.writeValueAsString(event)
        )
        outboxMessageRepository.save(message)
    }

}

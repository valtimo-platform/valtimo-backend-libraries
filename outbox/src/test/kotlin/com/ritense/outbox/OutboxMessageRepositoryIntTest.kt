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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

class OutboxMessageRepositoryIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var platformTransactionManager: PlatformTransactionManager

    @Test
    fun `should skip reading locked messages from the outbox table`(): Unit = runBlocking {
        insertOutboxMessage("event 1")
        insertOutboxMessage("event 2")

        val outboxMessage1Ref = async(Dispatchers.IO) {
            TransactionTemplate(platformTransactionManager).execute {
                val outboxMessage = outboxMessageRepository.findOutboxMessage()
                Thread.sleep(1000)
                outboxMessage
            }
        }

        val outboxMessage2Ref = async(Dispatchers.IO) {
            TransactionTemplate(platformTransactionManager).execute {
                val outboxMessage = outboxMessageRepository.findOutboxMessage()
                Thread.sleep(1000)
                outboxMessage
            }
        }

        assertThat(outboxMessage1Ref.await()!!.message).isNotEqualTo(outboxMessage2Ref.await()!!.message)
    }
}

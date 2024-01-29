package com.ritense.inbox.consumer

import com.ritense.inbox.BaseIntegrationTest
import com.ritense.inbox.InboxHandlingService
import com.ritense.inbox.InboxSink
import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.UUID
import java.util.concurrent.TimeUnit

class InboxCloudEventConsumerIntTest @Autowired constructor(
    private val inboxSink: InboxSink,
    @MockBean private val inboxHandlingService: InboxHandlingService,
    private val inboxCloudEventConsumer: InboxCloudEventConsumer
) : BaseIntegrationTest() {
    @Test
    fun test() {
        val messages = mutableListOf<String>()

        whenever(inboxHandlingService.handle(any())).then {
            messages.add(it.getArgument(0))
        }

        val uuid = UUID.randomUUID().toString()
        inboxSink.tryEmitNext(uuid)

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
            .until {
                messages.contains(uuid)
        }
    }

}
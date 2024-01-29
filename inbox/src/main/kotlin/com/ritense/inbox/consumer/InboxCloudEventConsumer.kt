package com.ritense.inbox.consumer

import com.ritense.inbox.InboxHandlingService
import java.util.function.Consumer

class InboxCloudEventConsumer(
    private val inboxHandlingService: InboxHandlingService): Consumer<String> {
    override fun accept(message: String) {
        inboxHandlingService.handle(message)
    }
}
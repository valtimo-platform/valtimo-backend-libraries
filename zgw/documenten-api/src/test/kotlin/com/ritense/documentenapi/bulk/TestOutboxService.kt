package com.ritense.documentenapi.bulk

import com.ritense.outbox.OutboxService
import com.ritense.outbox.domain.BaseEvent
import org.assertj.core.api.Assertions.assertThat
import java.util.function.Supplier

/**
 * Implementation of the outbox service used for testing (validation) purposes.
 * This is now built specifically for the bulk documentenapi but it can be used elsewhere as well.
 */
class TestOutboxService : OutboxService {
    private val _publishedEvents = mutableListOf<BaseEvent>()
    @Suppress("MemberVisibilityCanBePrivate")
    val publishedEvents get() = _publishedEvents.toList()

    override fun send(eventSupplier: Supplier<BaseEvent>) {
        _publishedEvents.add(eventSupplier.get())
    }

    fun clear() {
        _publishedEvents.clear()
    }

    fun assertEquals(vararg events: BaseEvent) {
        assertThat(publishedEvents)
            .usingRecursiveComparison()
            .ignoringFields("id", "date")
            .isEqualTo(events.toList())
    }
}
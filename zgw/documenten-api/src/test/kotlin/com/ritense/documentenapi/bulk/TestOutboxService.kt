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
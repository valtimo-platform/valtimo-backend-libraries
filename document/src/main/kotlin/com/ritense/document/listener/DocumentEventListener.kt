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

package com.ritense.document.listener

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.document.domain.event.CaseAssignedEvent
import com.ritense.document.domain.event.CaseCreatedEvent
import com.ritense.document.domain.event.CaseUnassignedEvent
import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.document.event.DocumentAssigneeChangedEvent
import com.ritense.document.event.DocumentUnassignedEvent
import com.ritense.valtimo.web.sse.messaging.RedisMessagePublisher
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import org.springframework.context.event.EventListener
import org.springframework.transaction.event.TransactionalEventListener

class DocumentEventListener(
    private val redisMessagePublisher: RedisMessagePublisher
) {

    @TransactionalEventListener(DocumentCreatedEvent::class)
    fun handleDocumentCreatedEvent(event: DocumentCreatedEvent) {
        val caseCreatedEvent = CaseCreatedEvent(event.documentId().id)
        val caseCreatedEventJson = jsonMapper().writeValueAsString(caseCreatedEvent)
        redisMessagePublisher.publish(caseCreatedEventJson)
    }

    @EventListener(DocumentUnassignedEvent::class)
    fun handleDocumentUnassignedEvent() {
        //redisMessagePublisher.publish(CaseUnassignedEvent().eventType)
    }

    @EventListener(DocumentAssigneeChangedEvent::class)
    fun handleDocumentAssignedEvent() {
        //redisMessagePublisher.publish(CaseAssignedEvent().eventType)
    }
}
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

package com.ritense.document.listener

import com.ritense.document.domain.event.CaseAssignedEvent
import com.ritense.document.domain.event.CaseCreatedEvent
import com.ritense.document.domain.event.CaseUnassignedEvent
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DocumentEventListenerTest {

    lateinit var subscriptionService: SseSubscriptionService
    lateinit var documentEventListener :DocumentEventListener

    @BeforeEach
    fun init(){
        subscriptionService = mock()
        documentEventListener = DocumentEventListener(subscriptionService)
    }

    @Test
    fun `should send sse event for a case created`(){
        documentEventListener.handleDocumentCreatedEvent()
        verify(subscriptionService).notifySubscribers(any<CaseCreatedEvent>())
    }

    @Test
    fun `should send sse event for a case assignment`(){
        documentEventListener.handleDocumentAssignedEvent()
        verify(subscriptionService).notifySubscribers(any<CaseAssignedEvent>())
    }

    @Test
    fun `should send sse event for a case unassignment`(){
        documentEventListener.handleDocumentUnassignedEvent()
        verify(subscriptionService).notifySubscribers(any<CaseUnassignedEvent>())
    }

}
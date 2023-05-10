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
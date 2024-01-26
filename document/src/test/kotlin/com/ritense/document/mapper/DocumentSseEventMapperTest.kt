package com.ritense.document.mapper

import com.ritense.document.domain.event.CaseAssignedEvent
import com.ritense.document.domain.event.CaseCreatedEvent
import com.ritense.document.domain.event.CaseUnassignedEvent
import com.ritense.document.event.DocumentAssigned
import com.ritense.document.event.DocumentCreated
import com.ritense.document.event.DocumentUnassigned
import com.ritense.inbox.ValtimoEvent
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DocumentSseEventMapperTest {

    lateinit var documentSseEventMapper: DocumentSseEventMapper

    @BeforeEach
    fun init(){
        documentSseEventMapper = DocumentSseEventMapper()
    }

    @Test
    fun `should map to sse event for a case created`(){
        val valtimoEvent = mock<ValtimoEvent>()
        whenever(valtimoEvent.type).thenReturn(DocumentCreated.TYPE)

        val sseEvent = documentSseEventMapper.map(valtimoEvent)
        assertInstanceOf(CaseCreatedEvent::class.java, sseEvent)
    }

    @Test
    fun `should map to sse event for a case assigned`() {
        val valtimoEvent = mock<ValtimoEvent>()
        whenever(valtimoEvent.type).thenReturn(DocumentAssigned.TYPE)

        val sseEvent = documentSseEventMapper.map(valtimoEvent)
        assertInstanceOf(CaseAssignedEvent::class.java, sseEvent)
    }

    @Test
    fun `should map to sse event for a case unassigned`() {
        val valtimoEvent = mock<ValtimoEvent>()
        whenever(valtimoEvent.type).thenReturn(DocumentUnassigned.TYPE)

        val sseEvent = documentSseEventMapper.map(valtimoEvent)
        assertInstanceOf(CaseUnassignedEvent::class.java, sseEvent)
    }

    @Test
    fun `should map to null with unknown type event`() {
        val valtimoEvent = mock<ValtimoEvent>()
        whenever(valtimoEvent.type).thenReturn("unknown")

        val sseEvent = documentSseEventMapper.map(valtimoEvent)
        assertNull(sseEvent)
    }
}
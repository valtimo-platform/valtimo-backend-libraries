package com.ritense.note.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.note.BaseIntegrationTest
import com.ritense.note.domain.Note
import com.ritense.note.event.NoteCreated
import com.ritense.note.event.NoteDeleted
import com.ritense.note.event.NoteUpdated
import com.ritense.note.event.NoteViewed
import com.ritense.note.event.NotesListed
import com.ritense.note.exception.NoteNotFoundException
import com.ritense.outbox.OutboxService
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.test.context.support.WithMockUser
import java.util.UUID
import java.util.function.Supplier

class NoteServiceIntTest() : BaseIntegrationTest() {
    @Autowired
    lateinit var noteService: NoteService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @SpyBean
    lateinit var outboxService: OutboxService

    lateinit var documentId: UUID

    @BeforeEach
    fun beforeEach() {
        documentId = AuthorizationContext.runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(PROFILE_DOCUMENT_DEFINITION_NAME, objectMapper.createObjectNode())
            ).resultingDocument().get().id()!!.id
        }

        val admin = ValtimoUser()
        admin.id = USERNAME
        admin.username = USERNAME
        admin.roles = listOf(USER, ADMIN)
        whenever(userManagementService.currentUser).thenReturn(admin)
        whenever(userManagementService.findByUserIdentifier(USERNAME)).thenReturn(admin)
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outbox message when creating note`() {
        reset(outboxService)

        val note = AuthorizationContext.runWithoutAuthorization {
            noteService.createNote(JsonSchemaDocumentId.newId(documentId), "test")
        }
        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService).send(eventCapture.capture())
        val firstEventValue = eventCapture.firstValue.get()

        Assertions.assertThat(firstEventValue).isInstanceOf(NoteCreated::class.java)
        Assertions.assertThat(firstEventValue.resultId).isEqualTo(note.id.toString())
        Assertions.assertThat(firstEventValue.result).isEqualTo(objectMapper.valueToTree(note))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outbox message when getting note by id`() {
        val createdNote = AuthorizationContext.runWithoutAuthorization {
            noteService.createNote(JsonSchemaDocumentId.newId(documentId), "test")
        }

        reset(outboxService)

        val note = AuthorizationContext.runWithoutAuthorization {
            noteService.getNoteById(createdNote.id)
        }

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService).send(eventCapture.capture())
        val firstEventValue = eventCapture.firstValue.get()

        Assertions.assertThat(firstEventValue).isInstanceOf(NoteViewed::class.java)
        Assertions.assertThat(firstEventValue.resultId).isEqualTo(note.id.toString())
        Assertions.assertThat(firstEventValue.result).isEqualTo(objectMapper.valueToTree(note))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should not send outbox message when trying to get note that does not exist`() {
        reset(outboxService)

        assertThrows<NoteNotFoundException> {
            AuthorizationContext.runWithoutAuthorization {
                noteService.getNoteById(UUID.randomUUID())
            }
        }

        verify(outboxService, times(0)).send(any());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outbox message when deleting note`() {
        val note = AuthorizationContext.runWithoutAuthorization {
            noteService.createNote(JsonSchemaDocumentId.newId(documentId), "test")
        }

        reset(outboxService)

        AuthorizationContext.runWithoutAuthorization {
            noteService.deleteNote(note.id)
        }

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, times(2)).send(eventCapture.capture())
        val lastEventValue = eventCapture.lastValue.get()

        Assertions.assertThat(lastEventValue).isInstanceOf(NoteDeleted::class.java)
        Assertions.assertThat(lastEventValue.resultId).isEqualTo(note.id.toString())
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outbox message when updating note`() {
        val createdNote = AuthorizationContext.runWithoutAuthorization {
            noteService.createNote(JsonSchemaDocumentId.newId(documentId), "test")
        }

        reset(outboxService)

        val updatedNote = AuthorizationContext.runWithoutAuthorization {
            noteService.editNote(createdNote.id, "test2")
        }
        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, times(2)).send(eventCapture.capture())
        val lastEventValue = eventCapture.lastValue.get()

        Assertions.assertThat(lastEventValue).isInstanceOf(NoteUpdated::class.java)
        Assertions.assertThat(lastEventValue.resultId).isEqualTo(updatedNote.id.toString())
        Assertions.assertThat(lastEventValue.result).isEqualTo(objectMapper.valueToTree(updatedNote))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outbox message when retrieving notes`() {
        val note = AuthorizationContext.runWithoutAuthorization {
            noteService.createNote(JsonSchemaDocumentId.newId(documentId), "test")
        }

        reset(outboxService)

        AuthorizationContext.runWithoutAuthorization {
            noteService.getNotes(note.documentId)
        }

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService).send(eventCapture.capture())
        val firstEventValue = eventCapture.firstValue.get()
        val eventNoteList: List<Note> = objectMapper.readValue(firstEventValue.result.toString())

        Assertions.assertThat(firstEventValue).isInstanceOf(NotesListed::class.java)
        Assertions.assertThat(firstEventValue.resultId).isEqualTo(null)
        Assertions.assertThat(eventNoteList.firstOrNull { it.id == note.id }).isNotNull
    }

    companion object {
        private const val USERNAME = "john@ritense.com"
        private const val PROFILE_DOCUMENT_DEFINITION_NAME = "profile"
    }
}
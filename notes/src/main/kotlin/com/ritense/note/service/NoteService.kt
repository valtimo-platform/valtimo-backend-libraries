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

package com.ritense.note.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.note.domain.Note
import com.ritense.note.event.NoteCreated
import com.ritense.note.event.NoteCreatedEvent
import com.ritense.note.event.NoteDeleted
import com.ritense.note.event.NoteDeletedEvent
import com.ritense.note.event.NoteUpdated
import com.ritense.note.event.NoteUpdatedEvent
import com.ritense.note.event.NoteViewed
import com.ritense.note.event.NotesListed
import com.ritense.note.exception.NoteNotFoundException
import com.ritense.note.repository.NoteRepository
import com.ritense.note.repository.SpecificationHelper
import com.ritense.outbox.OutboxService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


@Transactional
@Service
@SkipComponentScan
class NoteService(
    private val noteRepository: NoteRepository,
    private val userManagementService: UserManagementService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val authorizationService: AuthorizationService,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper
) {

    fun getNotes(
        documentId: UUID,
        pageable: Pageable = Pageable.unpaged(),
    ): Page<Note> {
        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                Note::class.java,
                NoteActionProvider.VIEW_LIST
            ),
            null
        )

        val notesPage: Page<Note> = noteRepository.findAll(spec.and(SpecificationHelper.byDocumentId(documentId)), pageable)

        outboxService.send {
            NotesListed(
                objectMapper.valueToTree(notesPage.content)
            )
        }

        return notesPage
    }

    fun createNote(
        documentId: JsonSchemaDocumentId,
        noteContent: String,
    ): Note {
        logger.debug { "Create note for document $documentId" }
        val user = userManagementService.currentUser
        val note = noteRepository.save(Note(documentId, user, noteContent))
        requirePermission(note, NoteActionProvider.CREATE)
        applicationEventPublisher.publishEvent(NoteCreatedEvent(documentId.id, note.id))

        outboxService.send {
            NoteCreated(
                note.id.toString(),
                objectMapper.valueToTree(note)
            )
        }

        return note
    }

    fun editNote(noteId: UUID, noteContent: String): Note {
        logger.debug { "Update note with id '$noteId'" }
        val note = getNoteById(noteId)
        requirePermission(note, NoteActionProvider.MODIFY)
        val copiedNote = note.copy(content = noteContent)
        val updatedNote = noteRepository.save(copiedNote)
        applicationEventPublisher.publishEvent(NoteUpdatedEvent(noteId))

        outboxService.send {
            NoteUpdated(
                updatedNote.id.toString(),
                objectMapper.valueToTree(updatedNote)
            )
        }

        return updatedNote
    }

    fun deleteNote(noteId: UUID) {
        logger.debug { "Delete note with id '$noteId'" }
        val note = getNoteById(noteId)
        requirePermission(note, NoteActionProvider.DELETE)
        noteRepository.deleteById(noteId)
        applicationEventPublisher.publishEvent(NoteDeletedEvent(noteId))
        outboxService.send {
            NoteDeleted(
                noteId.toString()
            )
        }
    }

    fun getNoteById(noteId: UUID): Note {
        val note = noteRepository.findById(noteId)

        if (note.isPresent) outboxService.send {
            NoteViewed(
                noteId.toString(),
                objectMapper.valueToTree(note)
            )
        }

        return note.orElseThrow { NoteNotFoundException(noteId) }
    }

    private fun requirePermission(note: Note, action: Action<Note>) {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                Note::class.java,
                action,
                note
            )
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

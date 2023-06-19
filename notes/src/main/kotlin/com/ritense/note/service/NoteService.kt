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

package com.ritense.note.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationService
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.note.domain.Note
import com.ritense.note.event.NoteCreatedEvent
import com.ritense.note.event.NoteDeletedEvent
import com.ritense.note.event.NoteUpdatedEvent
import com.ritense.note.exception.NoteNotFoundException
import com.ritense.note.repository.NoteRepository
import com.ritense.note.repository.SpecificationHelper
import com.ritense.valtimo.contract.authentication.UserManagementService
import java.util.UUID
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@Transactional
class NoteService(
    private val noteRepository: NoteRepository,
    private val userManagementService: UserManagementService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val authorizationService: AuthorizationService,
) {

    @Transactional(readOnly = true)
    fun getNotes(
        documentId: UUID,
        pageable: Pageable = Pageable.unpaged(),
    ): Page<Note> {
        val spec = authorizationService.getAuthorizationSpecification(
            AuthorizationRequest(
                Note::class.java,
                NoteActionProvider.LIST_VIEW
            ),
            null
        )
        return noteRepository.findAll(spec.and(SpecificationHelper.byDocumentId(documentId)), pageable)
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
        return note
    }

    fun editNote(noteId: UUID, noteContent: String): Note {
        logger.debug { "Update note with id '$noteId'" }
        val note = getNoteById(noteId)
        requirePermission(note, NoteActionProvider.MODIFY)
        val copiedNote = note.copy(content = noteContent)
        val updatedNote = noteRepository.save(copiedNote)
        applicationEventPublisher.publishEvent(NoteUpdatedEvent(noteId))
        return updatedNote
    }

    fun deleteNote(noteId: UUID) {
        logger.debug { "Delete note with id '$noteId'" }
        val note = getNoteById(noteId)
        requirePermission(note, NoteActionProvider.DELETE)
        noteRepository.deleteById(noteId)
        applicationEventPublisher.publishEvent(NoteDeletedEvent(noteId))
    }

    private fun getNoteById(noteId: UUID): Note {
        return noteRepository.findById(noteId).orElseThrow { NoteNotFoundException(noteId) }
    }

    private fun requirePermission(note: Note, action: Action<Note>) {
        authorizationService.requirePermission(
            AuthorizationRequest(
                Note::class.java,
                action
            ),
            note
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

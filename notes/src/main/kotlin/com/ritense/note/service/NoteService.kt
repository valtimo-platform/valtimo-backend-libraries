/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.note.domain.Note
import com.ritense.note.event.NoteCreatedEvent
import com.ritense.note.repository.NoteRepository
import com.ritense.valtimo.contract.authentication.CurrentUserService
import com.ritense.valtimo.contract.utils.SecurityUtils
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher

class NoteService(
    private val noteRepository: NoteRepository,
    private val currentUserService: CurrentUserService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    fun createNote(
        documentId: JsonSchemaDocumentId,
        noteContent: String,
    ): Note {
        logger.debug { "Create note for document $documentId" }
        SecurityUtils.getCurrentUserLogin()
        val user = currentUserService.currentUser
        val node = noteRepository.save(Note(documentId, user, noteContent))
        applicationEventPublisher.publishEvent(NoteCreatedEvent(documentId.id, node.id))
        return node
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

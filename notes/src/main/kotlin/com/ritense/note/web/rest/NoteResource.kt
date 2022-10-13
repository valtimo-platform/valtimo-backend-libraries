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

package com.ritense.note.web.rest

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.note.service.NoteService
import com.ritense.note.web.rest.dto.NoteCreateRequestDto
import com.ritense.note.web.rest.dto.NoteResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/document/{documentId}/note", produces = [MediaType.APPLICATION_JSON_VALUE])
class NoteResource(
    private val noteService: NoteService,
    private val documentService: DocumentService,
) {
    @GetMapping
    fun getNotes(
        @PathVariable(name = "documentId") documentId: UUID,
        @PageableDefault(
            sort = ["createdDate"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable = Pageable.unpaged()
    ): ResponseEntity<Page<NoteResponseDto>> {
        val notes = noteService.getNotes(
            documentId, pageable
        )

        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(documentId)

        if (!documentService.currentUserCanAccessDocument(jsonSchemaDocumentId)) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(notes.map { note -> NoteResponseDto(note) })
    }

    @PostMapping
    fun createNote(
        @PathVariable(name = "documentId") documentId: UUID,
        @RequestBody noteDto: NoteCreateRequestDto
    ): ResponseEntity<NoteResponseDto> {

        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(documentId)

        if (!documentService.currentUserCanAccessDocument(jsonSchemaDocumentId)) {
            return ResponseEntity.badRequest().build()
        }
        val note = noteService.createNote(
            jsonSchemaDocumentId,
            noteDto.content,
        )
        return ResponseEntity.ok(NoteResponseDto(note))
    }
}

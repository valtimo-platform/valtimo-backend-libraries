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

package com.ritense.note.repository

import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.authorization.permission.Permission
import com.ritense.note.domain.Note
import com.ritense.note.service.NoteService
import com.ritense.valtimo.contract.database.QueryDialectHelper

class NoteSpecificationFactory(
    private val noteService: NoteService,
    private var queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecificationFactory<Note> {

    override fun create(
        request: AuthorizationRequest<Note>,
        permissions: List<Permission>
    ): AuthorizationSpecification<Note> {
        return NoteSpecification(
            request,
            permissions,
            noteService,
            queryDialectHelper
        )
    }

    override fun canCreate(context: AuthorizationRequest<*>, permissions: List<Permission>): Boolean {
        return Note::class.java == context.resourceType
    }
}
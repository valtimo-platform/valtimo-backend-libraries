package com.ritense.note.repository

import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.authorization.permission.Permission
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.JsonSchemaDocumentSpecification
import com.ritense.note.domain.Note
import com.ritense.valtimo.contract.database.QueryDialectHelper

class NoteSpecificationFactory(
    private var queryDialectHelper: QueryDialectHelper
): AuthorizationSpecificationFactory<Note> {

    override fun create(
        context: AuthorizationRequest<Note>,
        permissions: List<Permission>
    ): AuthorizationSpecification<Note> {
        return NoteSpecification(
            permissions,
            context,
            queryDialectHelper
        )
    }

    override fun canCreate(context: AuthorizationRequest<*>): Boolean {
        return Note::class.java == context.resourceType
    }
}
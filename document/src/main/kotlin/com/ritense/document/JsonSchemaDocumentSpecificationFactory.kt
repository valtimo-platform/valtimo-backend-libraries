package com.ritense.document

import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.authorization.permission.Permission
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.JsonSchemaDocumentSpecification
import com.ritense.valtimo.contract.database.QueryDialectHelper

class JsonSchemaDocumentSpecificationFactory(
    private var queryDialectHelper: QueryDialectHelper
): AuthorizationSpecificationFactory<JsonSchemaDocument> {

    override fun create(
        context: AuthorizationRequest<JsonSchemaDocument>,
        permissions: List<Permission>
    ): AuthorizationSpecification<JsonSchemaDocument> {
        return JsonSchemaDocumentSpecification(
            permissions,
            context,
            queryDialectHelper
        )
    }

    override fun canCreate(context: AuthorizationRequest<*>): Boolean {
        return JsonSchemaDocument::class.java == context.resourceType
    }
}
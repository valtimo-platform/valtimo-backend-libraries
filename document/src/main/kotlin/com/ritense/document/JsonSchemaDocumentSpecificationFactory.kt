package com.ritense.document

import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationFilter
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.JsonSchemaDocumentSpecification
import com.ritense.valtimo.contract.database.QueryDialectHelper

class JsonSchemaDocumentSpecificationFactory(
    private var queryDialectHelper: QueryDialectHelper
): AuthorizationSpecificationFactory<JsonSchemaDocument> {

    override fun create(context: AuthorizationRequest<JsonSchemaDocument>): AuthorizationSpecification<JsonSchemaDocument> {
        return JsonSchemaDocumentSpecification(
            listOf(
                AuthorizationFilter("$.voornaam", "Peter", "AND"),
                AuthorizationFilter("documentDefinitionId.name", "leningen", "AND") // can also result in an increased nr of results
            ),
            queryDialectHelper
        )
    }

    override fun canCreate(context: AuthorizationRequest<*>): Boolean {
        return JsonSchemaDocument::class.java == context.classContext
    }
}
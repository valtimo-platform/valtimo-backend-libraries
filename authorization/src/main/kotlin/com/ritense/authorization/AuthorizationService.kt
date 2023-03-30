package com.ritense.authorization

import com.ritense.authorization.permission.ExpressionOperator
import com.ritense.authorization.permission.ExpressionPermissionFilter
import com.ritense.authorization.permission.FieldPermissionFilter
import com.ritense.authorization.permission.Permission

class AuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>
) {
    fun <T> requirePermission(context: AuthorizationRequest<T>, entity: T) {
        if (!(getAuthorizationSpecification(context).isAuthorized(context, entity)))
            throw RuntimeException("Unauthorized")
    }

    fun <T> getAuthorizationSpecification(context: AuthorizationRequest<T>): AuthorizationSpecification<T> {
        return (authorizationSpecificationFactories.first {
            it.canCreate(context)
        } as AuthorizationSpecificationFactory<T>).create(context, createExamplePermissions())
    }

    private fun createExamplePermissions(): List<Permission> {
        return listOf(
            Permission(
                Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                Action.LIST_VIEW,
                listOf()
            ),
            Permission(
                Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                Action.VIEW,
                listOf(
                    FieldPermissionFilter("documentDefinitionId.name", "leningen"),
                    ExpressionPermissionFilter(
                        "content.content",
                        "$.voornaam",
                        ExpressionOperator.EQUAL_TO, "Peter")
                )
            )
            /*            Permission(
                            "document-definition",
                            Action.ASSIGN,
                            listOf(FieldPermissionFilter("documentDefinitionId.name", "leningen"))
                        ),
                        Permission(
                            "task-definition",
                            Action.VIEW,
                            listOf(
                                FieldPermissionFilter("taskDefinition", "controle-aanvraag")
                            )
                        ),
                        Permission( // This is generally not recommended, but can be done.
                            "task-definition",
                            Action.COMPLETE,
                            listOf(
                                FieldPermissionFilter("taskDefinition", "controle-aanvraag"),
                                ContainerPermissionFilter(
                                    "document-definition",
                                    listOf(
                                        FieldPermissionFilter("documentDefinitionId.name", "leningen"),
                                        ExpressionPermissionFilter(
                                            "content",
                                            "$.amount",
                                            "<",
                                            "10000"
                                        )
                                    )
                                )
                            )
                        )*/
        )
    }
}
package com.ritense.authorization

import com.ritense.authorization.permission.ContainerPermissionFilter
import com.ritense.authorization.permission.ExpressionOperator
import com.ritense.authorization.permission.ExpressionPermissionFilter
import com.ritense.authorization.permission.FieldPermissionFilter
import com.ritense.authorization.permission.Permission

class AuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
    private val mappers: List<AuthorizationEntityMapper<*, *>>
) {
    fun <T : Any> requirePermission(context: AuthorizationRequest<T>, entity: T, permissions: List<Permission>?) {

        if (!(getAuthorizationSpecification(context, permissions).isAuthorized(entity)))
            throw RuntimeException("Unauthorized")
    }

    fun <T : Any> getAuthorizationSpecification(
        context: AuthorizationRequest<T>,
        permissions: List<Permission>?
    ): AuthorizationSpecification<T> {
        return (authorizationSpecificationFactories.first {
            it.canCreate(context)
        } as AuthorizationSpecificationFactory<T>).create(context, permissions ?: createExamplePermissions())
    }

    fun <FROM, TO> getMapper(from: Class<FROM>, to: Class<TO>): AuthorizationEntityMapper<FROM, TO> {
        return mappers.first {
            it.appliesTo(from, to)
        } as AuthorizationEntityMapper<FROM, TO>
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
            ),
            Permission(
                Class.forName("com.ritense.note.domain.Note"),
                Action.VIEW,
                listOf(
                    ContainerPermissionFilter(
                        Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                        listOf(
                            FieldPermissionFilter("documentDefinitionId.name", "leningen"),
                            ExpressionPermissionFilter(
                                "content.content",
                                "$.voornaam",
                                ExpressionOperator.EQUAL_TO, "Peter")
                        )
                    )
                )
            ),
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
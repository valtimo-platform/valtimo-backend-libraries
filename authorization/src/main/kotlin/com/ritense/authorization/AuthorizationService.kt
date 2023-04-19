package com.ritense.authorization

import com.ritense.authorization.permission.ContainerPermissionCondition
import com.ritense.authorization.permission.PermissionExpressionOperator
import com.ritense.authorization.permission.ExpressionPermissionCondition
import com.ritense.authorization.permission.FieldPermissionCondition
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
                emptyList()
            ),
            Permission(
                Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                Action.CLAIM,
                listOf(
                    FieldPermissionCondition("documentDefinitionId.name", "leningen"),
                    ExpressionPermissionCondition(
                        "content.content",
                        "$.height",
                        PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java)
                )
            ),
            Permission(
                Class.forName("com.ritense.note.domain.Note"),
                Action.VIEW,
                listOf(
                    ContainerPermissionCondition(
                        Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                        listOf(
                            FieldPermissionCondition("documentDefinitionId.name", "leningen"),
                            ExpressionPermissionCondition(
                                "content.content",
                                "$.height",
                                PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java),
                            FieldPermissionCondition("assigneeFullName", "Asha Miller")
                        )
                    )
                )
            ),
        )
    }
}
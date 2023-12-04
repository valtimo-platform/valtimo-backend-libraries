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

package com.ritense.authorization.specification

import com.ritense.authorization.Action
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.request.RelatedEntityAuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.condition.ContainerPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.condition.PermissionCondition
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

abstract class AuthorizationSpecification<T : Any>(
    protected val authRequest: AuthorizationRequest<T>,
    protected val permissions: List<Permission>
) : Specification<T> {
    internal open fun isAuthorized(): Boolean {
        return when (authRequest) {
            is EntityAuthorizationRequest<T> -> isAuthorizedForEntity(authRequest)
            is RelatedEntityAuthorizationRequest<T> -> isAuthorizedForRelatedEntity(authRequest)
            else -> false
        }
    }

    private fun isAuthorizedForEntity(entityAuthorizationRequest: EntityAuthorizationRequest<T>): Boolean {
        if (entityAuthorizationRequest.entities.isEmpty()) {
            return false
        }
        val permissions = permissions.filter { permission ->
            entityAuthorizationRequest.resourceType == permission.resourceType && entityAuthorizationRequest.action == permission.action
        }
        return entityAuthorizationRequest.entities.all { entity ->
            permissions.any { permission -> permission.appliesTo(entityAuthorizationRequest.resourceType, entity) }
        }
    }

    private fun isAuthorizedForRelatedEntity(
        relatedEntityAuthorizationRequest: RelatedEntityAuthorizationRequest<T>
    ): Boolean {

        if (relatedEntityAuthorizationRequest.resourceType == relatedEntityAuthorizationRequest.relatedResourceType) {

            return isAuthorizedForEntity(
                EntityAuthorizationRequest(
                    relatedEntityAuthorizationRequest.resourceType,
                    relatedEntityAuthorizationRequest.action,
                    identifierToEntity(relatedEntityAuthorizationRequest.relatedResourceId)
                )
            )
        }

        return permissions
            .filter { permission ->
                relatedEntityAuthorizationRequest.resourceType == permission.resourceType
                    && relatedEntityAuthorizationRequest.action == permission.action
            }
            .firstOrNull { permission ->
                permission.conditionContainer.conditions.all { permissionCondition ->
                    isAuthorizedForRelatedEntityRecursive(
                        relatedEntityAuthorizationRequest,
                        permissionCondition
                    )
                }
            } != null
    }

    private fun isAuthorizedForRelatedEntityRecursive(
        relatedEntityAuthorizationRequest: RelatedEntityAuthorizationRequest<T>,
        permissionCondition: PermissionCondition
    ): Boolean {
        return if (permissionCondition is ContainerPermissionCondition<*>) {
            if (permissionCondition.resourceType == relatedEntityAuthorizationRequest.relatedResourceType) {
                this.findSpecification(relatedEntityAuthorizationRequest, permissionCondition).isAuthorized()
            } else {
                permissionCondition.conditions.all {
                    isAuthorizedForRelatedEntityRecursive(relatedEntityAuthorizationRequest, it)
                }
            }
        } else {
            true
        }
    }

    fun combinePredicates(criteriaBuilder: CriteriaBuilder, predicates: List<Predicate>): Predicate {
        return criteriaBuilder.or(*predicates.toTypedArray())
    }

    private fun <TO : Any> findSpecification(
        authRequest: RelatedEntityAuthorizationRequest<T>,
        container: ContainerPermissionCondition<TO>
    ): AuthorizationSpecification<TO> {
        return AuthorizationServiceHolder.currentInstance.getAuthorizationSpecification(
            RelatedEntityAuthorizationRequest(
                container.resourceType,
                Action(Action.IGNORE),
                authRequest.relatedResourceType,
                authRequest.relatedResourceId
            ),
            listOf(
                Permission(
                    resourceType = container.resourceType,
                    action = Action<Any>(Action.IGNORE),
                    conditionContainer = ConditionContainer(container.conditions),
                    role = Role(key = "")
                )
            )
        )
    }

    protected abstract fun identifierToEntity(identifier: String): T

    abstract override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate
}
/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.condition.ContainerPermissionCondition
import com.ritense.authorization.permission.condition.PermissionCondition
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.request.RelatedEntityAuthorizationRequest
import com.ritense.authorization.role.Role
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import mu.KotlinLogging
import org.springframework.data.jpa.domain.Specification

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
        val entities = entityAuthorizationRequest.entities.ifEmpty { listOf(null) }
        val permissions = permissions.filter { permission ->
            entityAuthorizationRequest.resourceType == permission.resourceType && entityAuthorizationRequest.action == permission.action
        }
        return entities.all { entity ->
            permissions.any { permission ->
                permission
                    .appliesTo(
                        entityAuthorizationRequest.resourceType,
                        entity,
                        entityAuthorizationRequest.context?.resourceType,
                        entityAuthorizationRequest.context?.entity
                    )
            }
        }
    }

    private fun isAuthorizedForRelatedEntity(
        relatedEntityAuthorizationRequest: RelatedEntityAuthorizationRequest<T>
    ): Boolean {

        if (relatedEntityAuthorizationRequest.resourceType == relatedEntityAuthorizationRequest.relatedResourceType) {
            val entity = try {
                identifierToEntity(relatedEntityAuthorizationRequest.relatedResourceId)
            } catch (e: NotImplementedError) {
                null
            } catch (e: Throwable) {
                logger.error { e }
                null
            }
            return isAuthorizedForEntity(
                EntityAuthorizationRequest(
                    relatedEntityAuthorizationRequest.resourceType,
                    relatedEntityAuthorizationRequest.action,
                    entity
                ).apply {
                    relatedEntityAuthorizationRequest.context?.let { context -> this.withContext(context) }
                }
            )
        }

        return permissions
            .filter { permission ->
                relatedEntityAuthorizationRequest.resourceType == permission.resourceType
                    && relatedEntityAuthorizationRequest.action == permission.action
            }
            .firstOrNull { permission ->
                permission.appliesInContext(
                    relatedEntityAuthorizationRequest.context?.resourceType,
                    relatedEntityAuthorizationRequest.context?.entity
                ) && permission.conditionContainer.conditions.all { permissionCondition ->
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

    /**
     * Creates a WHERE clause for a query of the referenced entity in form of a Predicate for the given Root and
     * CriteriaQuery. This is only used when applying predicates to the root query for the entity. It is recommended to
     * implement {@link #toPredicate(Root, AbstractQuery, CriteriaBuilder) toPredicate} instead to ensure filters are
     * also applied when the entity is used in a relation.
     *
     * @param root must not be {@literal null}.
     * @param query must not be {@literal null}.
     * @param criteriaBuilder must not be {@literal null}.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        return toPredicate(root, query as AbstractQuery<*>, criteriaBuilder)
    }

    /**
     * Creates a WHERE clause for a query of the referenced entity in form of a Predicate for the given Root and
     * CriteriaQuery. This is used when applying predicates a subquery, and unless the default
     * {@link #toPredicate(Root, CriteriaQuery, CriteriaBuilder) toPredicate} is overridden, also to the root query.
     *
     * @param root must not be {@literal null}.
     * @param query must not be {@literal null}.
     * @param criteriaBuilder must not be {@literal null}.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    abstract fun toPredicate(
        root: Root<T>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
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

package com.ritense.authorization

import com.ritense.authorization.permission.Permission
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

abstract class AuthorizationSpecification<T : Any>(
    protected val authRequest: AuthorizationRequest<T>,
    protected val permissions: List<Permission>
) : Specification<T> {
    internal open fun isAuthorized(): Boolean {
        return when(authRequest) {
            is EntityAuthorizationRequest<T> -> isAuthorizedForEntity()
            is RelatedEntityAuthorizationRequest<T> -> isAuthorizedForRelatedEntity()
            else -> false
        }
    }

    private fun isAuthorizedForEntity(): Boolean { // TODO: See EntityAuthorizationRequest
        val entityAuthorizationRequest = authRequest as EntityAuthorizationRequest<T>
        return entityAuthorizationRequest.entity != null && permissions.filter { permission ->
            entityAuthorizationRequest.resourceType == permission.resourceType && entityAuthorizationRequest.action == permission.action
        }.any { permission ->
            permission.appliesTo(entityAuthorizationRequest.resourceType, entityAuthorizationRequest.entity)
        }
    }

    private fun isAuthorizedForRelatedEntity(): Boolean {
        val relatedEntityAuthorizationRequest = authRequest as RelatedEntityAuthorizationRequest<T>

        // Are the correct resource type and actions in the permissions? (should always be true, so aren't we double checking here for no reason?
        // Retrieve entity from table if root resource is equal to related resource (e.g. we've gone forther down the rabbit hole and are now at the correct related entity)

        var authRequest = EntityAuthorizationRequest(relatedEntityAuthorizationRequest.resourceType, relatedEntityAuthorizationRequest.action, entity)

        return AuthorizationSpecification<T : Any>().isAuthorized()


        return false
    }

    fun combinePredicates(criteriaBuilder: CriteriaBuilder, predicates: List<Predicate>): Predicate {
        return criteriaBuilder.or(*predicates.toTypedArray())
    }

    abstract fun identifierToEntity(vararg identifiers: Object): T

    abstract override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate
}
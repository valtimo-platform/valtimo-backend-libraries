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

package com.ritense.authorization.specification.impl

import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.specification.AuthorizationSpecification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class NoopAuthorizationSpecification<T : Any>(
    authRequest: AuthorizationRequest<T>,
    permissions: List<Permission>
) : AuthorizationSpecification<T>(
    authRequest,
    permissions
) {
    override fun isAuthorized(): Boolean {
        return AuthorizationContext.ignoreAuthorization
    }

    override fun toPredicate(root: Root<T>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(criteriaBuilder.literal(1), 1)
    }

    override fun identifierToEntity(identifier: String): T {
        throw NotImplementedError()
    }
}
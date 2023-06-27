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

import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.EntityAuthorizationRequest
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.authorization.permission.Permission

class NoopAuthorizationSpecificationFactory<T: Any> : AuthorizationSpecificationFactory<T> {
    override fun create(
            context: EntityAuthorizationRequest<T>,
            permissions: List<Permission>
    ): AuthorizationSpecification<T> {
        return NoopAuthorizationSpecification(context, permissions)
    }

    override fun canCreate(context: EntityAuthorizationRequest<*>, permissions: List<Permission>): Boolean {
        return AuthorizationContext.ignoreAuthorization
    }


}
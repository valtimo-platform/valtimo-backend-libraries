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

package com.ritense.valtimo.camunda.authorization

import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.authorization.specification.AuthorizationSpecificationFactory
import com.ritense.authorization.permission.Permission
import com.ritense.valtimo.camunda.domain.CamundaIdentityLink
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.service.CamundaTaskService

class CamundaIdentityLinkSpecificationFactory(
    private val camundaRuntimeService: CamundaRuntimeService,
    private var queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecificationFactory<CamundaIdentityLink> {

    override fun create(
            request: AuthorizationRequest<CamundaIdentityLink>,
            permissions: List<Permission>
    ): AuthorizationSpecification<CamundaIdentityLink> {
        return CamundaIdentityLinkSpecification(
            request,
            permissions,
            camundaRuntimeService,
            queryDialectHelper
        )
    }

    override fun canCreate(request: AuthorizationRequest<*>, permissions: List<Permission>): Boolean {
        return CamundaTask::class.java == request.resourceType
    }
}
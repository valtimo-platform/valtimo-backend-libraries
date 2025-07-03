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

package com.ritense.valtimo.service

import com.ritense.authorization.Action.Companion.deny
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.operaton.domain.OperatonBytearray
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.repository.OperatonByteArraySpecificationHelper.Companion.byDeploymentId
import com.ritense.valtimo.operaton.repository.OperatonByteArraySpecificationHelper.Companion.byName
import com.ritense.valtimo.operaton.repository.OperatonBytearrayRepository
import kotlin.jvm.optionals.getOrNull

class OperatonByteArrayService(
    private val operatonBytearrayRepository: OperatonBytearrayRepository,
    private val authorizationService: AuthorizationService,
) {

    fun getByNameAndDeploymentId(resourceName: String, deploymentId: String): OperatonBytearray {
        require(resourceName.contains(".")) { "Resource name $resourceName is not a file name" }
        denyAuthorization()
        return operatonBytearrayRepository.findOne(byName(resourceName).and(byDeploymentId(deploymentId))).getOrNull()
            ?: error("No Operaton bytes found for name '$resourceName' and deploymentId '$deploymentId'")
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                OperatonProcessDefinition::class.java,
                deny()
            )
        )
    }
}
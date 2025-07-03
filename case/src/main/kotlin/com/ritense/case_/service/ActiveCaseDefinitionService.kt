/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.case_.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case_.authorization.CaseDefinitionActionProvider
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class ActiveCaseDefinitionService(
    private val caseDefinitionService: CaseDefinitionService,
    private val authorizationService: AuthorizationService,
) {

    fun getActiveCaseDefinition(caseDefinitionKey: String): CaseDefinition {
        val activeCaseDefinition = runWithoutAuthorization {
            caseDefinitionService.getActiveCaseDefinition(caseDefinitionKey)
        }

        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseDefinition::class.java,
                CaseDefinitionActionProvider.VIEW,
                activeCaseDefinition
            )
        )

        return activeCaseDefinition ?: throw UnknownCaseDefinitionException(caseDefinitionKey)
    }

    fun setGlobalActiveCaseDefinition(caseDefinitionId: CaseDefinitionId): CaseDefinition {
        return caseDefinitionService.setActiveCaseDefinition(caseDefinitionId)
    }
}
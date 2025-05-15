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

package com.ritense.case.service

import com.ritense.case.repository.CaseDefinitionSpecificationHelper.Companion.byActive
import com.ritense.case.repository.CaseDefinitionSpecificationHelper.Companion.byCaseDefinitionKey
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.importer.ImportContext
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class CaseDefinitionCheckerImpl(
    private val caseDefinitionRepository: CaseDefinitionRepository,
    private val environment: Environment,
    private val draftEnvironments: String,
) : CaseDefinitionChecker {

    override fun existsCaseDefinition(caseDefinitionId: CaseDefinitionId): Boolean {
        return caseDefinitionRepository.findById(caseDefinitionId) != null
    }

    override fun existsCaseDefinition(caseDefinitionKey: String): Boolean {
        return caseDefinitionRepository.findOne(
            byCaseDefinitionKey(caseDefinitionKey).and(byActive(true))
        ) != null
    }

    override fun canUpdateCaseDefinition(caseDefinitionId: CaseDefinitionId): Boolean {
        if (!canUpdateGlobalConfiguration()) {
            return false
        }
        val caseDefinition = caseDefinitionRepository.findById(caseDefinitionId).orElse(null)
            ?: return false
        return !caseDefinition.final
    }

    override fun canUpdateGlobalConfiguration(): Boolean {
        if (ImportContext.isImporting()) {
            return true
        }
        return isDraftEnvironment()
    }

    override fun assertCaseDefinitionExists(caseDefinitionId: CaseDefinitionId) {
        require(existsCaseDefinition(caseDefinitionId)) { "CaseDefinition $caseDefinitionId does not exist." }
    }

    override fun assertCaseDefinitionExists(caseDefinitionKey: String) {
        require(existsCaseDefinition(caseDefinitionKey)) { "CaseDefinition $caseDefinitionKey does not exist." }
    }

    override fun assertCanUpdateCaseDefinition(caseDefinitionId: CaseDefinitionId) {
        assertCanUpdateGlobalConfiguration()
        val caseDefinition = caseDefinitionRepository.findById(caseDefinitionId).orElse(null)
            ?: error("CaseDefinition $caseDefinitionId does not exist.")
        require(!caseDefinition.final) {
            "Failed to update CaseDefinition $caseDefinitionId. This case definition is final and therefore can't be updated."
        }
    }

    override fun assertCanCreateOrUpdateCaseDefinition(caseDefinitionId: CaseDefinitionId, final: Boolean) {
        val isDraftEnvironment = isDraftEnvironment()
        if (!final && !isDraftEnvironment) {
            error("Failed to create/update CaseDefinition $caseDefinitionId. This Valtimo environment does not support drafts. Missing one of the following Spring profiles: [$draftEnvironments]")
        }
        val existingCaseDefinition = caseDefinitionRepository.findById(caseDefinitionId).orElse(null)
        if (existingCaseDefinition != null && existingCaseDefinition.final) {
            error("Failed to update CaseDefinition $caseDefinitionId. This case definition is final.")
        }
    }

    override fun assertCanUpdateGlobalConfiguration() {
        require(canUpdateGlobalConfiguration()) {
            "Failed to update configuration. This Valtimo environment does not support drafts. Missing one of the following Spring profiles: [$draftEnvironments]"
        }
    }

    private fun isDraftEnvironment(): Boolean {
        return draftEnvironments.split(',').any { draftEnvironment ->
            environment.activeProfiles.any { it == draftEnvironment }
        }
    }
}

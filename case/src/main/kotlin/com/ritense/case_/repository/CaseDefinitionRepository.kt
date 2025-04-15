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

package com.ritense.case_.repository

import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.semver4j.Semver
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface CaseDefinitionRepository
    : JpaRepository<CaseDefinition, CaseDefinitionId>, JpaSpecificationExecutor<CaseDefinition> {
    fun findFirstByIdKeyOrderByIdVersionTagDesc(key: String): CaseDefinition?

    fun findAllByActiveIsTrue(pageable: Pageable): Page<CaseDefinition>

    fun findByActiveIsTrueAndIdKey(caseDefinitionKey: String): CaseDefinition?

    fun findAllByIdKeyOrderByIdVersionTagDesc(caseDefinitionKey: String): List<CaseDefinition>

    fun findAllByIdKeyAndBasedOnVersionTag(caseDefinitionKey: String, basedOnVersionTag: Semver?): List<CaseDefinition>

    fun findAllByFinalTrue(): List<CaseDefinition>

    fun existsByIdKey(caseDefinitionKey: String): Boolean

    @Query(value = "" +
        "SELECT c.id.versionTag " +
        "FROM CaseDefinition c " +
        "WHERE c.id.key = :key " +
        "ORDER BY c.id.versionTag DESC")
    fun findVersionsForCaseDefinitionKey(key: String): List<Semver>
}
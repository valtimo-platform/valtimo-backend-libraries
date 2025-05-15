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

package com.ritense.processdocument.repository

import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinition
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinitionId
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.lang.Nullable

interface ProcessDefinitionCaseDefinitionRepository:
    JpaRepository<ProcessDefinitionCaseDefinition, ProcessDefinitionCaseDefinitionId> {
    fun findByIdCaseDefinitionId(caseDefinitionId: CaseDefinitionId): List<ProcessDefinitionCaseDefinition>
    fun findByIdProcessDefinitionId(processDefinitionId: ProcessDefinitionId): ProcessDefinitionCaseDefinition
    fun findAllByIdCaseDefinitionIdAndIdProcessDefinitionIdId(
        caseDefinitionId: CaseDefinitionId,
        processDefinitionId: String
    ): List<ProcessDefinitionCaseDefinition>

    @Query(
        ("SELECT  pdcd " +
            "FROM    ProcessDefinitionCaseDefinition pdcd " +
            "WHERE   pdcd.id.caseDefinitionId = :caseDefinitionId " +
            "AND (:startableByUser IS NULL OR pdcd.startableByUser = :startableByUser)" +
            "AND (:canInitializeDocument IS NULL OR pdcd.canInitializeDocument = :canInitializeDocument)")
    )
    fun findAll(
        @Param("caseDefinitionId") caseDefinitionId: CaseDefinitionId,
        @Nullable @Param("startableByUser") startableByUser: Boolean?,
        @Nullable @Param("canInitializeDocument") canInitializeDocument: Boolean?
    ): List<ProcessDefinitionCaseDefinition>

}
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

package com.ritense.document.repository

import com.ritense.document.domain.CaseTag
import com.ritense.document.domain.CaseTagId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CaseTagRepository : JpaRepository<CaseTag, CaseTagId> {
    fun findByIdCaseDefinitionNameOrderByOrder(caseDefinitionName: String): List<CaseTag>
    fun findDistinctByIdCaseDefinitionNameAndIdKey(caseDefinitionName: String, key: String): CaseTag?
    fun existsByIdCaseDefinitionNameAndIdKey(caseDefinitionName: String, key: String): Boolean
    @Query(
        value = """
            SELECT CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM case_tag_link
                    WHERE case_tag_key = :caseTagKey
                    AND case_definition_name = :caseDefinitionName
                ) THEN 'true'
                ELSE 'false'
            END
        """, nativeQuery = true
    )
    fun isCaseTagInUse(
        @Param("caseTagKey") caseTagKey: String,
        @Param("caseDefinitionName") caseDefinitionName: String
    ): Boolean
}
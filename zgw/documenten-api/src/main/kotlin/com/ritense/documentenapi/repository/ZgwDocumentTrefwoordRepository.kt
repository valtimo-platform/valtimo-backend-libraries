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

package com.ritense.documentenapi.repository

import com.ritense.documentenapi.domain.ZgwDocumentTrefwoord
import com.ritense.documentenapi.domain.ZgwDocumentTrefwoordId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ZgwDocumentTrefwoordRepository : JpaRepository<ZgwDocumentTrefwoord, ZgwDocumentTrefwoordId> {
    fun findAllByCaseDefinitionName(caseDefinitionName: String, pageable: Pageable): Page<ZgwDocumentTrefwoord>

    fun findAllByCaseDefinitionNameAndValue(caseDefinitionName: String, value: String): ZgwDocumentTrefwoord?

    fun deleteByCaseDefinitionNameAndValue(caseDefinitionName: String, value: String)
}
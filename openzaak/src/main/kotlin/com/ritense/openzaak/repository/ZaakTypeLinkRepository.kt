/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.repository

import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ZaakTypeLinkRepository : JpaRepository<ZaakTypeLink, ZaakTypeLinkId> {

    fun findByDocumentDefinitionName(documentDefinitionName: String): ZaakTypeLink?

    fun findByDocumentDefinitionNameIn(documentDefinitionNames: List<String>): List<ZaakTypeLink?>

    @Modifying
    @Query("" +
            "   DELETE " +
            "   FROM    ZaakTypeLink ztl " +
            "   WHERE   ztl.documentDefinitionName = :documentDefinitionName "
    )
    fun deleteByDocumentDefinitionName(
        @Param("documentDefinitionName") documentDefinitionName: String
    )

}
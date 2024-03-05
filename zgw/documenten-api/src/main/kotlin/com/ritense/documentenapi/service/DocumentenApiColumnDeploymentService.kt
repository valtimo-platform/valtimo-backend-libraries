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

package com.ritense.documentenapi.service

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.AUTEUR
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.BESTANDSOMVANG
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.CREATIEDATUM
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.INFORMATIEOBJECTTYPE
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.TITEL
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional

open class DocumentenApiColumnDeploymentService(
    private val documentenApiService: DocumentenApiService,
) {

    @Transactional
    @RunWithoutAuthorization
    @EventListener(DocumentDefinitionDeployedEvent::class)
    open fun createCaseTabs(event: DocumentDefinitionDeployedEvent) {
        if (event.documentDefinition().id().version() == 1L) {
            getDefaultColumns(event.documentDefinition().id().name()).forEach { column ->
                documentenApiService.updateColumn(column)
            }
        }
    }

    private fun getDefaultColumns(documentDefinitionName: String): List<DocumentenApiColumn> {
        return listOf(
            DocumentenApiColumn(DocumentenApiColumnId(documentDefinitionName, TITEL)),
            DocumentenApiColumn(DocumentenApiColumnId(documentDefinitionName, CREATIEDATUM)),
            DocumentenApiColumn(DocumentenApiColumnId(documentDefinitionName, AUTEUR)),
            DocumentenApiColumn(DocumentenApiColumnId(documentDefinitionName, BESTANDSOMVANG)),
            DocumentenApiColumn(DocumentenApiColumnId(documentDefinitionName, INFORMATIEOBJECTTYPE)),
        )
    }
}
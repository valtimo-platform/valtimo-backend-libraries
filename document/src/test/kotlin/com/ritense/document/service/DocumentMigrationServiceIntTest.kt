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

package com.ritense.document.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.DocumentMigrationPatch
import com.ritense.document.domain.DocumentMigrationRequest
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.Companion.byDocumentDefinitionIdName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@Transactional
class DocumentMigrationServiceIntTest @Autowired constructor(
    private val documentMigrationService: DocumentMigrationService
) : BaseIntegrationTest() {

    @Test
    fun `should migrate document`() {
        createDocument(
            definitionOf("referenced"),
            """{"address": {"streetName": "Straatnaam"}}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = "referenced",
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = "allows-all",
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "/address/streetName", target = "/address"),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName("allows-all"))[0]
        assertEquals("""{"address":"Straatnaam"}""", targetDocument.content().asJson().toString())
        assertEquals("allows-all", targetDocument.definitionId().name())
    }
}
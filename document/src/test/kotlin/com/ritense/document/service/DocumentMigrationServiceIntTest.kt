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
    fun `should migrate document from referenced schema`() {
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

    @Test
    fun `should migrate document to referenced schema`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "referenced"
        createDocument(
            definitionOf(sourceDefinition),
            """{"address":"Straatnaam"}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "/address", target = "/address/streetName"),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName(targetDefinition))[0]
        assertEquals("""{"address":{"streetName":"Straatnaam"}}""", targetDocument.content().asJson().toString())
        assertEquals(targetDefinition, targetDocument.definitionId().name())
    }

    @Test
    fun `should migrate document from schema with array`() {
        val sourceDefinition = "array-example"
        val targetDefinition = "allows-all"
        createDocument(
            definitionOf(sourceDefinition),
            """{"files":[{"id":"1"},{"id":"2"}]}}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "/files/0/id", target = "/firstFileId"),
                        DocumentMigrationPatch(source = "/files/1/id", target = "/secondFileId"),
                        DocumentMigrationPatch(source = "/files", target = ""),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName(targetDefinition))[0]
        assertEquals("""{"firstFileId":"1","secondFileId":"2"}""", targetDocument.content().asJson().toString())
        assertEquals(targetDefinition, targetDocument.definitionId().name())
    }

    @Test
    fun `should migrate document to schema with array`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "array-example"
        createDocument(
            definitionOf(sourceDefinition),
            """{"firstFileId":1,"secondFileId":2,"thirdFileId":3}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "/firstFileId", target = "/files/0/id"),
                        DocumentMigrationPatch(source = "/secondFileId", target = "/files/1/id"),
                        DocumentMigrationPatch(source = "/thirdFileId", target = "/files/2/id"),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName(targetDefinition))[0]
        assertEquals("""{"files":[{"id":"1"},{"id":"2"},{"id":"3"}]}""", targetDocument.content().asJson().toString())
        assertEquals(targetDefinition, targetDocument.definitionId().name())
    }

    @Test
    fun `should set default values and keep source type`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "allows-all"
        createDocument(
            definitionOf(sourceDefinition),
            """{}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "Text", target = "/string"),
                        DocumentMigrationPatch(source = "", target = "/emptyString"),
                        DocumentMigrationPatch(source = "\${null}", target = "/null"),
                        DocumentMigrationPatch(source = "100", target = "/numberAsString"),
                        DocumentMigrationPatch(source = "\${100}", target = "/number"),
                        DocumentMigrationPatch(source = "\${3.14}", target = "/decimal"),
                        DocumentMigrationPatch(source = "\${true}", target = "/boolean"),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName(targetDefinition))[0]
        assertEquals(
            """{"string":"Text","emptyString":"","null":null,"numberAsString":"100","number":100,"decimal":3.14,"boolean":true}""",
            targetDocument.content().asJson().toString()
        )
        assertEquals(targetDefinition, targetDocument.definitionId().name())
    }

    @Test
    fun `should set default values and convert to target type`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "all-types"
        createDocument(
            definitionOf(sourceDefinition),
            """{}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "Text", target = "/string"),
                        DocumentMigrationPatch(source = "null", target = "/null"),
                        DocumentMigrationPatch(source = "100", target = "/integer"),
                        DocumentMigrationPatch(source = "3.14", target = "/number"),
                        DocumentMigrationPatch(source = "true", target = "/boolean"),
                        DocumentMigrationPatch(source = "[5]", target = "/array"),
                        DocumentMigrationPatch(source = """{"a":2}""", target = "/object"),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName(targetDefinition))[0]
        assertEquals(
            """{"string":"Text","null":null,"integer":100,"number":3.14,"boolean":true,"array":[5],"object":{"a":2}}""",
            targetDocument.content().asJson().toString()
        )
        assertEquals(targetDefinition, targetDocument.definitionId().name())
    }

    @Test
    fun `should get and convert spel value`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "all-types"
        createDocument(
            definitionOf(sourceDefinition),
            """{"a":{"b":"6"}}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "/a", target = ""),
                        DocumentMigrationPatch(source = "\${source.a.b}", target = "/number"),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName(targetDefinition))[0]
        assertEquals("""{"number":6}""", targetDocument.content().asJson().toString())
        assertEquals(targetDefinition, targetDocument.definitionId().name())
    }

    @Test
    fun `should move array`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "referenced-array"
        createDocument(
            definitionOf(sourceDefinition),
            """{"customer":{"addresses":[{"streetName":"Grosthuizen"}]}}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "/customer/addresses", target = "/addresses"),
                        DocumentMigrationPatch(source = "/customer", target = ""),
                    )
                )
            )
        }

        val targetDocument = documentRepository.findAll(byDocumentDefinitionIdName(targetDefinition))[0]
        assertEquals("""{"addresses":[{"streetName":"Grosthuizen"}]}""", targetDocument.content().asJson().toString())
        assertEquals(targetDefinition, targetDocument.definitionId().name())
    }
}
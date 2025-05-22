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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.DocumentMigrationPatch
import com.ritense.document.domain.DocumentMigrationRequest
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.Optional
import kotlin.test.assertEquals

@Transactional
class DocumentMigrationServiceTest {

    private val documentDefinitionService: JsonSchemaDocumentDefinitionService = mock()
    private val documentRepository: JsonSchemaDocumentRepository = mock()
    private val applicationContext: ApplicationContext = mock()
    private val objectMapper: ObjectMapper = MapperSingleton.get()

    private lateinit var documentMigrationService: DocumentMigrationService

    @BeforeEach
    fun beforeEach() {
        documentMigrationService = DocumentMigrationService(
            documentDefinitionService,
            documentRepository,
            applicationContext,
            objectMapper,
        )

        whenever(documentDefinitionService.findBy(any())).thenAnswer { answer ->
            val definitionId = answer.getArgument(0, JsonSchemaDocumentDefinitionId::class.java)
            val schema = loadSchema(definitionId.name())
            Optional.of(JsonSchemaDocumentDefinition(definitionId, schema))
        }
    }

    @Test
    fun `should migrate document from referenced schema`() {
        newDocument(
            "referenced",
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


        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals("""{"address":"Straatnaam"}""", documentContent)
    }

    @Test
    fun `should migrate document to referenced schema`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "referenced"
        newDocument(
            sourceDefinition,
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

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals("""{"address":{"streetName":"Straatnaam"}}""", documentContent)
    }

    @Test
    fun `should migrate document from schema with array`() {
        val sourceDefinition = "array-example"
        val targetDefinition = "allows-all"
        newDocument(
            sourceDefinition,
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

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals("""{"firstFileId":"1","secondFileId":"2"}""", documentContent)
    }

    @Test
    fun `should migrate document to schema with array`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "array-example"
        newDocument(
            sourceDefinition,
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

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals("""{"files":[{"id":"1"},{"id":"2"},{"id":"3"}]}""", documentContent)
    }

    @Test
    fun `should set default values and keep source type`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "allows-all"
        newDocument(
            sourceDefinition,
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

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals(
            """{"string":"Text","emptyString":"","null":null,"numberAsString":"100","number":100,"decimal":3.14,"boolean":true}""",
            documentContent
        )
    }

    @Test
    fun `should set default values and convert to target type`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "all-types"
        newDocument(
            sourceDefinition,
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

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals(
            """{"string":"Text","null":null,"integer":100,"number":3.14,"boolean":true,"array":[5],"object":{"a":2}}""",
            documentContent
        )
    }

    @Test
    fun `should get and convert spel value`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "all-types"
        newDocument(
            sourceDefinition,
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
                        DocumentMigrationPatch(source = "\${'null'}", target = "/string"),
                        DocumentMigrationPatch(source = "\${'null'}", target = "/null"),
                        DocumentMigrationPatch(source = "\${source.a.b}", target = "/number"),
                        DocumentMigrationPatch(source = "/a", target = ""),
                    )
                )
            )
        }

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals("""{"string":"null","null":null,"number":6}""", documentContent)
    }

    @Test
    fun `should move array`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "referenced-array"
        newDocument(
            sourceDefinition,
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

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals("""{"addresses":[{"streetName":"Grosthuizen"}]}""", documentContent)
    }

    @Test
    fun `should not move missing node`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "employee"
        newDocument(
            sourceDefinition,
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
                        DocumentMigrationPatch(source = "/firstName", target = "/personalInformation/firstName"),
                    )
                )
            )
        }

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals("""{}""", documentContent)
    }

    @Test
    fun `should allow SpEL expression`() {
        val sourceDefinition = "allows-all"
        val targetDefinition = "allows-all"
        newDocument(
            sourceDefinition,
            """{"firstName":"John","lastName":"Doe"}"""
        )

        runWithoutAuthorization {
            documentMigrationService.migrateDocuments(
                DocumentMigrationRequest(
                    documentDefinitionNameSource = sourceDefinition,
                    documentDefinitionVersionSource = 1,
                    documentDefinitionNameTarget = targetDefinition,
                    documentDefinitionVersionTarget = 1,
                    patches = listOf(
                        DocumentMigrationPatch(source = "/firstName", target = ""),
                        DocumentMigrationPatch(source = "/lastName", target = ""),
                        DocumentMigrationPatch(
                            source = "\${source.firstName+' '+source.lastName}", target = "/fullName"
                        ),
                        DocumentMigrationPatch(source = "\${'Welcome '+target.fullName}", target = "/welcomeMsg"),
                    )
                )
            )
        }

        val captor = argumentCaptor<JsonSchemaDocument>()
        verify(documentRepository).save(captor.capture())
        val documentContent = captor.firstValue.content().asJson().toString()
        assertEquals(
            """{"fullName":"John Doe","welcomeMsg":"Welcome John Doe"}""",
            documentContent
        )
    }

    fun newDocument(definitionName: String, documentContent: String) {
        val definitionId = JsonSchemaDocumentDefinitionId.newId(definitionName)
        val definitionSchema = loadSchema(definitionName)
        val definition = JsonSchemaDocumentDefinition(definitionId, definitionSchema)
        val sequenceGenerator: DocumentSequenceGeneratorService = mock()
        whenever(sequenceGenerator.next(any())).thenReturn(1)
        val document = JsonSchemaDocument.create(
            definition,
            JsonDocumentContent(documentContent),
            "SYSTEM",
            sequenceGenerator,
            mock(),
        ).resultingDocument().get()
        whenever(documentRepository.findAll(any<Specification<JsonSchemaDocument>>())).thenReturn(listOf(document))
    }

    private fun loadSchema(definitionName: String): JsonSchema {
        return try {
            JsonSchema.fromResourceUri(URI.create("config/document/definition/$definitionName.schema.json"))
        } catch (_: Exception) {
            JsonSchema.fromResourceUri(URI.create("config/document/definition/noautodeploy/$definitionName.schema.json"))
        }
    }
}
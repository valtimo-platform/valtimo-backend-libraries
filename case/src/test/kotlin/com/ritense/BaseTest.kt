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

package com.ritense

import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.document.TestHelper
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocument.CreateDocumentResultImpl
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaRelatedFile
import com.ritense.document.service.DocumentSequenceGeneratorService
import com.ritense.valtimo.contract.annotation.AllOpen
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.case_.CaseDefinitionId.Companion.of
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.semver4j.Semver
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@AllOpen
class BaseTest(
    val documentSequenceGeneratorService: DocumentSequenceGeneratorService = mock(),
    val testHelper: TestHelper = TestHelper(),
) {

    init {
        whenever(documentSequenceGeneratorService.next(ArgumentMatchers.any())).thenReturn(1L)
    }

    protected fun definition(): JsonSchemaDocumentDefinition {
        val jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.of("house", caseDefinitionId())
        val schema = JsonSchema.fromResourceUri(
            path(
                jsonSchemaDocumentDefinitionId.caseDefinitionId(),
                jsonSchemaDocumentDefinitionId.name()
            )
        )
        return JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, schema)
    }

    protected fun definitionOf(name: String?): JsonSchemaDocumentDefinition {
        val documentDefinitionName = JsonSchemaDocumentDefinitionId.of(name, caseDefinitionId())
        val schema = JsonSchema.fromResourceUri(
            path(
                documentDefinitionName.caseDefinitionId(),
                documentDefinitionName.name()
            )
        )
        return JsonSchemaDocumentDefinition(documentDefinitionName, schema)
    }

    protected fun definitionOf(documentDefinitionId: JsonSchemaDocumentDefinitionId): JsonSchemaDocumentDefinition {
        val schema = JsonSchema.fromResourceUri(
            path(
                documentDefinitionId.caseDefinitionId(),
                documentDefinitionId.name()
            )
        )
        return JsonSchemaDocumentDefinition(documentDefinitionId, schema)
    }

    protected fun definitionOfForUnitTests(name: String?): JsonSchemaDocumentDefinition {
        val documentDefinitionName = JsonSchemaDocumentDefinitionId.of(name, caseDefinitionId())
        val schema = JsonSchema.fromResourceUri(
            testHelper!!.path(
                documentDefinitionName.name()
            )
        )
        return JsonSchemaDocumentDefinition(documentDefinitionName, schema)
    }

    protected fun caseDefinitionId(): CaseDefinitionId {
        return of("house", "1.0.0")
    }

    protected fun createDocument(): JsonSchemaDocument {
        val json = "{\"firstName\": \"John\"}"
        val content = JsonDocumentContent(json)
        return createDocument(content)
    }

    protected fun createDocument(content: JsonDocumentContent?): JsonSchemaDocument {
        return JsonSchemaDocument
            .create(definition(), content, USERNAME, documentSequenceGeneratorService, null)
            .resultingDocument()
            .orElseThrow()
    }

    protected fun createDocument(
        definition: JsonSchemaDocumentDefinition?,
        content: JsonDocumentContent?
    ): CreateDocumentResultImpl {
        return JsonSchemaDocument.create(definition, content, USERNAME, documentSequenceGeneratorService, null)
    }

    protected fun relatedFile(): JsonSchemaRelatedFile {
        return JsonSchemaRelatedFile(
            UUID.randomUUID(),
            "Some-Name",
            1L,
            LocalDateTime.now(),
            "some-body"
        )
    }

    fun path(caseDefinitionId: CaseDefinitionId, name: String): URI {
        val caseDefinitionVersion = caseDefinitionId.versionTag
        val formattedCaseDefinitionVersion = caseDefinitionVersion.major.toString() +
            "-" + caseDefinitionVersion.minor +
            "-" + caseDefinitionVersion.patch
        return URI.create(
            String.format(
                "config/case/%s/%s/document/definition/%s.json",
                caseDefinitionId.key,
                formattedCaseDefinitionVersion,
                "$name.schema"
            )
        )
    }

    fun caseDefinition(
        id: CaseDefinitionId = CaseDefinitionId("key", "1.0.0"),
        name: String = "name",
        active: Boolean = true,
        canHaveAssignee: Boolean = false,
        autoAssignTasks: Boolean = false,
        hasExternalStartForm: Boolean = false,
        externalStartFormUrl: String? = null,
    ): CaseDefinition {
        return CaseDefinition(
            id = id,
            name = name,
            description = "description",
            createdBy = "system",
            createdDate = LocalDateTime.now(),
            basedOnVersionTag = Semver.parse("1.0.0-SNAPSHOT"),
            final = true,
            active = active,

            canHaveAssignee = canHaveAssignee,
            autoAssignTasks = autoAssignTasks,
            hasExternalStartForm = hasExternalStartForm,
            externalStartFormUrl = externalStartFormUrl,
        )
    }

    companion object{
        const val USERNAME: String = "test@test.com"
    }
}
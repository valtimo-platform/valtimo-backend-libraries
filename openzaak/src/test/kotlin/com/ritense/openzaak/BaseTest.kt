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

package com.ritense.openzaak

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.DocumentSequenceGeneratorService
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.service.impl.OpenZaakConfigService
import com.ritense.openzaak.service.impl.OpenZaakTokenGeneratorService
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Optional

abstract class BaseTest {

    var openZaakTokenGeneratorService: OpenZaakTokenGeneratorService = OpenZaakTokenGeneratorService()

    @Mock
    lateinit var documentSequenceGeneratorService: DocumentSequenceGeneratorService

    @Mock
    lateinit var openZaakConfigService: OpenZaakConfigService

    @Mock
    lateinit var restTemplate: RestTemplate

    @Mock
    lateinit var zaakService: ZaakService

    @Mock
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @Mock
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService

    @Mock
    lateinit var documentService: DocumentService

    lateinit var document: JsonSchemaDocument

    fun baseSetUp() {
        MockitoAnnotations.openMocks(this)

        whenever(documentSequenceGeneratorService.next(any())).thenReturn(1)

        val documentOptional = documentOptional()
        document = documentOptional.orElseThrow()

        whenever(openZaakConfigService.getOpenZaakConfig()).thenReturn(openzaakConfig())
        whenever(documentService.findBy(any())).thenReturn(documentOptional)
    }

    fun openzaakConfig(): OpenZaakConfig {
        return OpenZaakConfig(
            "https://openzaak.ritense.com/",
            "valtimo_openzaak_test",
            "ySCrWMK7nCPdoSkjydb58racw2tOzuDqgge3SFhgR3Fe",
            Rsin("002564440")
        )
    }

    fun documentOptional(): Optional<JsonSchemaDocument> {
        return JsonSchemaDocument.create(
            definition(),
            JsonDocumentContent("{\"name\": \"whatever\" }"),
            "USERNAME",
            documentSequenceGeneratorService,
            null
        ).resultingDocument()
    }

    private fun definition(): JsonSchemaDocumentDefinition {
        val jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("house")
        val jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()))
        return JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema)
    }

    fun httpHeaders(): HttpHeaders {
        val header = HttpHeaders()
        header.contentType = MediaType.APPLICATION_JSON
        return header
    }

    fun path(name: String): URI {
        return URI.create(String.format("config/document/definition/%s.json", "$name.schema"))
    }

}
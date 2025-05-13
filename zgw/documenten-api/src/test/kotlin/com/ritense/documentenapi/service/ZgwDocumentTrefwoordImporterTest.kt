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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.repository.ZgwDocumentTrefwoordRepository
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class ZgwDocumentTrefwoordImporterTest(
    @Mock private val zgwDocumentTrefwoordRepository: ZgwDocumentTrefwoordRepository,
    @Mock private val zgwDocumentTrefwoordService: ZgwDocumentTrefwoordService,
    @Mock private val objectMapper: ObjectMapper
) {
    private lateinit var importer: ZgwDocumentTrefwoordImporter

    @BeforeEach
    fun before() {
        importer =
            ZgwDocumentTrefwoordImporter(zgwDocumentTrefwoordRepository, zgwDocumentTrefwoordService, objectMapper)
    }

    @Test
    fun `should be of type 'zgw-document-trefwoord'`() {
        assertThat(importer.type()).isEqualTo("zgwdocumenttrefwoord")
    }

    @Test
    fun `should depend on 'documentdefinition' type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(DOCUMENT_DEFINITION))
    }

    @Test
    fun `should support zgw-document-trefwoord fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-zgw-document-trefwoord fileName`() {
        assertThat(importer.supports("config/case/trefwoorden/x/my-case.zgw-document-trefwoorden.json")).isFalse()
        assertThat(importer.supports("config/case/trefwoorden/my-zgw-document-trefwoorden.json")).isFalse()
    }

    private companion object {
        const val FILENAME = "/zgw/trefwoord/my-case.zgw-document-trefwoord.json"
    }
}
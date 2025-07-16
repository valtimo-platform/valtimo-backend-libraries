/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.iko.service.IkoRepositoryService
import com.ritense.importer.ValtimoImportTypes.Companion.GLOBAL_FORM
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class IkoRepositoryConfigImporterTest(
    @Mock private val objectMapper: ObjectMapper,
    @Mock private val ikoRepositoryService: IkoRepositoryService,
) {
    private lateinit var importer: IkoRepositoryConfigImporter

    @BeforeEach
    fun before() {
        importer = IkoRepositoryConfigImporter(objectMapper, ikoRepositoryService)
    }

    @Test
    fun `should be of type 'ikorepositoryconfig'`() {
        assertThat(importer.type()).isEqualTo("ikorepositoryconfig")
    }

    @Test
    fun `should depend on 'globalform' and 'form' type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(GLOBAL_FORM))
    }

    @Test
    fun `should support iko-repository-config fileName`() {
        assertThat(importer.supports("/global/iko/objecten-api.iko-repository-config.json")).isTrue()
    }

    @Test
    fun `should not support non-iko-repository-config fileName`() {
        assertThat(importer.supports("/case/iko/objecten-api.iko-repository-config.json")).isFalse()
        assertThat(importer.supports("/global/iko/objecten-api.non-iko-repository-config.json")).isFalse()
    }
}
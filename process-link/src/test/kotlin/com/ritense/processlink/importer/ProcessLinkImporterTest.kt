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

package com.ritense.processlink.importer

import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DEFINITION
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ProcessLinkImporterTest(
    @Mock private val processLinkService: ProcessLinkService,
    @Mock private val repositoryService: CamundaRepositoryService
) {
    private lateinit var importer: ProcessLinkImporter

    @BeforeEach
    fun before() {
        whenever(processLinkService.getImporterDependsOnTypes()).thenReturn(setOf("x", "y", "z"))

        importer = ProcessLinkImporter(
            processLinkService,
            repositoryService,
            MapperSingleton.get()
        )
    }

    @Test
    fun `should be of type 'processlink'`() {
        assertThat(importer.type()).isEqualTo("processlink")
    }

    @Test
    fun `should depend on 'processdefinition' type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(PROCESS_DEFINITION, "x", "y", "z"))
    }

    @Test
    fun `should support processlink fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-processlink fileName`() {
        assertThat(importer.supports("my-processlink.json")).isFalse()
        assertThat(importer.supports("test.xml")).isFalse()
    }

    private companion object {
        const val FILENAME = "config/x/y/z/my.processlink.json"
    }
}
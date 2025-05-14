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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.changelog.service.ChangelogService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CaseTaskListImporterTest(
    @Mock private val objectMapper: ObjectMapper,
    @Mock private val taskColumnService: TaskColumnService,
) {
    private lateinit var importer: CaseTaskListImporter

    @BeforeEach
    fun before() {
        importer = CaseTaskListImporter(objectMapper, taskColumnService)
    }

    @Test
    fun `should be of type 'casetasklist'`() {
        assertThat(importer.type()).isEqualTo("casetasklist")
    }

    @Test
    fun `should depend on 'documentdefinition' type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(DOCUMENT_DEFINITION))
    }

    @Test
    fun `should support caseTaskList fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-caseTaskList fileName`() {
        assertThat(importer.supports("/case/task-list/x/test.case-task-list.json")).isFalse()
        assertThat(importer.supports("/case/task-list/test-json")).isFalse()
    }

    private companion object {
        const val FILENAME = "/case/task-list/my-doc-def.case-task-list.json"
    }
}
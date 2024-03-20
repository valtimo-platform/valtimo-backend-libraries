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

package com.ritense.case.deployment

import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.TAB_ORDER
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionName
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
class CaseTaskListDeploymentServiceIT @Autowired constructor(
    private val changesetRepository: ChangesetRepository,
    private val taskListColumnRepository: TaskListColumnRepository,
    private val changelogDeployer: ChangelogDeployer
) : BaseIntegrationTest() {

    @Test
    fun `should auto deploy case task list changeset from resource folder`() {
        taskListColumnRepository.deleteAll()
        whenever(caseTaskListDeploymentService.getPath()).thenCallRealMethod()
        changesetRepository.deleteAllByKey("case-task-list")

        changelogDeployer.deployAll()

        val changeset = changesetRepository.findById("some-case-type.case-task-list")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/some-case-type.case-task-list.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5))
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isNotNull()

        val columns = taskListColumnRepository
            .findByIdCaseDefinitionNameOrderByOrderAsc("some-case-type")

        assertEquals("first-name", columns[0].id.key)
        assertEquals("test:firstName", columns[0].path)
        assertEquals("enum", columns[0].displayType.type)
        assertEquals("First name", columns[0].title)
        assertEquals(0, columns[0].order)

        assertEquals("last-name", columns[1].id.key)
        assertEquals("test:lastName", columns[1].path)
        assertEquals("enum", columns[1].displayType.type)
        assertEquals("Last name", columns[1].title)
        assertEquals(1, columns[1].order)
    }
}
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

package com.ritense.documentenapi.deployment

import com.ritense.documentenapi.BaseIntegrationTest
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
class ZgwDocumentListColumnDeploymentServiceIT @Autowired constructor(
    private val changesetRepository: ChangesetRepository,
    private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    private val changelogDeployer: ChangelogDeployer
) : BaseIntegrationTest() {
    @Test
    fun `should auto deploy documenten api column changeset from resource folder`() {
        documentenApiColumnRepository.deleteAll()
        changesetRepository.deleteAllByKey("case-documenten-api-column")

        whenever(zgwDocumentListColumnDeploymentService.getPath()).thenReturn("classpath*:**/1-add-profile.zgw-document-list-column.json")

        changelogDeployer.deployAll()

        val changeset = changesetRepository.findById("profile.zgw-document-list-columns-v1")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/1-add-profile.zgw-document-list-column.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5))
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isNotNull()

        val columns = documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder("profile")

        assertThat(columns.size).isEqualTo(6)
        assertThat(columns[0].id.key.toString()).isEqualTo("AUTEUR")
        assertThat(columns[0].id.caseDefinitionName).isEqualTo("profile")
        assertThat(columns[0].order).isEqualTo(0)


        assertThat(columns[5].id.key.toString()).isEqualTo("BESTANDSNAAM")
        assertThat(columns[5].id.caseDefinitionName).isEqualTo("profile")
        assertThat(columns[5].order).isEqualTo(5)
    }

    @Test
    fun `should replace documenten api columns for case after deploying the same case definition`() {
        documentenApiColumnRepository.deleteAll()
        changesetRepository.deleteAllByKey("case-documenten-api-column")

        whenever(zgwDocumentListColumnDeploymentService.getPath()).thenReturn("classpath*:**/*-add-profile.zgw-document-list-column.json")

        changelogDeployer.deployAll()

        val changeset = changesetRepository.findById("profile.zgw-document-list-columns-v2")

        assertThat(changeset.isPresent).isTrue()

        val columns = documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder("profile")
        assertThat(columns.size).isEqualTo(3)

        assertThat(columns[2].id.caseDefinitionName).isEqualTo("profile")
        assertThat(columns[2].id.key.toString()).isEqualTo("VERSIE")
        assertThat(columns[2].order).isEqualTo(2)
    }
}
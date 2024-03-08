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
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
class ZgwDocumentListColumnDeploymentServiceIT @Autowired constructor(
    private val changesetRepository: ChangesetRepository,
    private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    private val changelogDeployer: ChangelogDeployer
) : BaseIntegrationTest() {
    @Autowired
    lateinit var documentenApiService: DocumentenApiService

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `should auto deploy documenten api column changeset from resource folder`() {
        documentenApiColumnRepository.deleteAll()
        changesetRepository.deleteAllByKey("case-documenten-api-column")

        changelogDeployer.deployAll()

        val changeset = changesetRepository.findById("profile.zgw-document-list-columns-v1")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/profile.zgw-document-list-column.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5))
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isNotNull()
//
        val columns = documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder("profile")
//
        assertThat(columns.size).isEqualTo(6)
        assertThat(columns[0].id.key.toString()).isEqualTo("AUTEUR")
        assertThat(columns[0].id.caseDefinitionName).isEqualTo("profile")
        assertThat(columns[0].order).isEqualTo(0)


        assertThat(columns[5].id.key.toString()).isEqualTo("BESTANDSNAAM")
        assertThat(columns[5].id.caseDefinitionName).isEqualTo("profile")
        assertThat(columns[5].order).isEqualTo(5)
    }
//
//    @Test
//    fun `should replace tabs for case after deploying the same case definition`() {
//        caseTabRepository.deleteAll()
//        whenever(caseTabDeploymentService.getPath()).thenReturn("classpath*:**/tabs-update-v*.json")
//
//        changelogDeployer.deployAll()
//
//        val tabs = caseTabRepository.findAll()
//        assertThat(tabs.size).isEqualTo(1)
//
//        assertThat(tabs[0].name).isEqualTo("Standard")
//        assertThat(tabs[0].id.key).isEqualTo("standard")
//        assertThat(tabs[0].type).isEqualTo(CaseTabType.STANDARD)
//        assertThat(tabs[0].contentKey).isEqualTo("standard")
//    }
//
//    @Test
//    fun `should add tabs for other case definition`() {
//        caseTabRepository.deleteAll()
//        whenever(caseTabDeploymentService.getPath()).thenReturn("classpath*:**/tabs-add-v*.json")
//
//        changelogDeployer.deployAll()
//
//        val tabs = caseTabRepository.findAll()
//        assertThat(tabs.size).isEqualTo(2)
//    }
//
//    @Test
//    fun `should fail deploying tabs for non existing case type`() {
//        caseTabRepository.deleteAll()
//        whenever(caseTabDeploymentService.getPath()).thenReturn("classpath*:**/tabs-fail.json")
//
//        val exception = assertThrows<IllegalStateException> {
//            changelogDeployer.deployAll()
//        }
//
//        val tabs = caseTabRepository.findAll()
//        assertThat(tabs.size).isEqualTo(0)
//        assertThat(exception.message).isEqualTo("Failed to execute changelog: test/config/case-tabs/tabs-fail.json")
//        assertThat(exception.cause).isInstanceOf(NoSuchElementException::class.java)
//        assertThat(exception.cause?.message).isEqualTo("Case definition with name some-case-type-that-does-not-exist does not exist!")
//    }
//
//    @Test
//    fun `should add tabs for deployed case definition`() {
//        val tabs = caseTabRepository.findAll(byCaseDefinitionName("house"), Sort.by(TAB_ORDER))
//        assertThat(tabs.size).isEqualTo(5)
//        assertThat(tabs[0].contentKey).isEqualTo("summary")
//        assertThat(tabs[1].contentKey).isEqualTo("progress")
//        assertThat(tabs[2].contentKey).isEqualTo("audit")
//        assertThat(tabs[3].contentKey).isEqualTo("documents")
//        assertThat(tabs[4].contentKey).isEqualTo("notes")
//    }
}
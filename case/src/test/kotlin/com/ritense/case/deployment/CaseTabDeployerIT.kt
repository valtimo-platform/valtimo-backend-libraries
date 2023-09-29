/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
class CaseTabDeployerIT : BaseIntegrationTest() {
    @Autowired
    lateinit var changesetRepository: ChangesetRepository

    @Autowired
    lateinit var caseTabRepository: CaseTabRepository

    @Autowired
    lateinit var changelogDeployer: ChangelogDeployer

    @BeforeEach
    fun setUp() {
        caseTabRepository.deleteAll()
    }

    @Test
    fun `should auto deploy tabs changeset from resource folder`() {
        whenever(caseTabDeployer.getPath()).thenCallRealMethod()
        changesetRepository.deleteAllByKey("case-tab")

        changelogDeployer.deployAll()

        val changeset = changesetRepository.findById("case-tabs-deploy-test")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/autodeploy-test.case-tabs.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5))
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isNotNull()

        val tabs = caseTabRepository.findAll(byCaseDefinitionName("some-case-type"), Sort.by(TAB_ORDER))

        assertThat(tabs[0].name).isEqualTo("Standard")
        assertThat(tabs[0].id.key).isEqualTo("standard")
        assertThat(tabs[0].type).isEqualTo(CaseTabType.STANDARD)
        assertThat(tabs[0].content).isEqualTo("standard")

        assertThat(tabs[1].name).isEqualTo("Custom tab")
        assertThat(tabs[1].id.key).isEqualTo("custom-tab")
        assertThat(tabs[1].type).isEqualTo(CaseTabType.CUSTOM)
        assertThat(tabs[1].content).isEqualTo("some-custom-component")
    }

    @Test
    fun `should replace tabs for case after deploying the same case definition`() {
        whenever(caseTabDeployer.getPath()).thenReturn("classpath*:**/tabs-update-v*.json")

        changelogDeployer.deployAll()

        val tabs = caseTabRepository.findAll()
        assertThat(tabs.size).isEqualTo(1)

        assertThat(tabs[0].name).isEqualTo("Standard")
        assertThat(tabs[0].id.key).isEqualTo("standard")
        assertThat(tabs[0].type).isEqualTo(CaseTabType.STANDARD)
        assertThat(tabs[0].content).isEqualTo("standard")
    }

    @Test
    fun `should add tabs for other case definition`() {
        whenever(caseTabDeployer.getPath()).thenReturn("classpath*:**/tabs-add-v*.json")

        changelogDeployer.deployAll()

        val tabs = caseTabRepository.findAll()
        assertThat(tabs.size).isEqualTo(2)
    }
}
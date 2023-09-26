package com.ritense.case.deployment

import com.ritense.case.BaseIntegrationTest
import com.ritense.case.repository.CaseTabRepository
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class CaseTabDeployerIT: BaseIntegrationTest() {
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
        changesetRepository.deleteAllByKey("case-key")

        changelogDeployer.deployAll()

        val changeset = changesetRepository.findById("case-tabs-deploy-test")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/autodeploy-test.case-tabs.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5))
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isNotNull()

        val tabs = caseTabRepository.findByIdCaseDefinitionName("some-case-type")

        assertThat(tabs[0].name).isEqualTo("Summary")
        assertThat(tabs[0].id.key).isEqualTo("summary")
        assertThat(tabs[0].type).isEqualTo("system")
        assertThat(tabs[0].content).isEqualTo("summary")

        assertThat(tabs[1].name).isEqualTo("Custom tab")
        assertThat(tabs[1].id.key).isEqualTo("custom-tab")
        assertThat(tabs[1].type).isEqualTo("custom")
        assertThat(tabs[1].content).isEqualTo("some-custom-component")
    }

    @Test
    fun `should replace tabs for case after deploying the same case definition`() {
        whenever(caseTabDeployer.getPath()).thenReturn("classpath*:**/tabs-update-v*.json")

        changelogDeployer.deployAll()

        val tabs = caseTabRepository.findAll()
        assertThat(tabs.size).isEqualTo(1)

        assertThat(tabs[0].name).isEqualTo("Summary")
        assertThat(tabs[0].id.key).isEqualTo("summary")
        assertThat(tabs[0].type).isEqualTo("system")
        assertThat(tabs[0].content).isEqualTo("summary")
    }

    @Test
    fun `should add tabs for other case definition`() {
        whenever(caseTabDeployer.getPath()).thenReturn("classpath*:**/tabs-add-v*.json")

        changelogDeployer.deployAll()

        val tabs = caseTabRepository.findAll()
        assertThat(tabs.size).isEqualTo(2)
    }
}
package com.ritense.dashboard.deployment

import com.ritense.dashboard.BaseIntegrationTest
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class DashboardDeployerIT: BaseIntegrationTest() {
    @Autowired
    lateinit var changesetRepository: ChangesetRepository

    @Autowired
    lateinit var dashboardRepository: DashboardRepository

    @Autowired
    lateinit var changelogDeployer: ChangelogDeployer

    @Test
    fun `should auto deploy dashboard changeset from resource folder`() {
        whenever(dashboardDeployer.getPath()).thenCallRealMethod()

        changelogDeployer.deployAll()

        val changeset = changesetRepository.findById("dashboard-auto-deploy-test")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/autodeploy-test.dashboard.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.now().minusSeconds(1), Instant.now().plusSeconds(1))
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isNotNull()
    }

    @Test
    fun `should auto deploy dashboard from resource folder`() {
        whenever(dashboardDeployer.getPath()).thenCallRealMethod()

        changelogDeployer.deployAll()

        val dashboard = dashboardRepository.findByKey("team-dashboard")!!

        assertThat(dashboard.key).isEqualTo("team-dashboard")
        assertThat(dashboard.title).isEqualTo("Team dashboard")
        assertThat(dashboard.order).isEqualTo(1)
        assertThat(dashboard.createdBy).isEqualTo("auto-deployed")
        assertThat(dashboard.description).isEqualTo("some description")
        assertThat(dashboard.widgetConfigurations.size).isEqualTo(1)
        assertThat(dashboard.widgetConfigurations[0].title).isEqualTo("Test widget")
        assertThat(dashboard.widgetConfigurations[0].key).isEqualTo("test-widget")
        assertThat(dashboard.widgetConfigurations[0].dataSourceKey).isEqualTo("test-key-single")
        assertThat(dashboard.widgetConfigurations[0].dataSourceProperties).isNotNull()
        assertThat(dashboard.widgetConfigurations[0].displayType).isEqualTo("number")
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties).isNotNull()
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties.get("subtitle").asText()).isEqualTo("Some test")
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties.get("useKPI").asBoolean()).isEqualTo(true)
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties.get("lowSeverityThreshold").asInt()).isEqualTo(5)
    }

    @Test
    fun `should update dashboard after new changeset`() {
        whenever(dashboardDeployer.getPath()).thenReturn("classpath*:**/dashboard-update-v*.json")

        changelogDeployer.deployAll()

        val dashboards = dashboardRepository.findAll()
        assertThat(dashboards.size).isEqualTo(1)
        val dashboard = dashboards[0]

        assertThat(dashboard.key).isEqualTo("team-dashboard")
        assertThat(dashboard.title).isEqualTo("Other dashboard")
        assertThat(dashboard.order).isEqualTo(1)
        assertThat(dashboard.createdBy).isEqualTo("auto-deployed")
        assertThat(dashboard.description).isEqualTo("some other description")
        assertThat(dashboard.widgetConfigurations.size).isEqualTo(1)
        assertThat(dashboard.widgetConfigurations[0].title).isEqualTo("Other widget test")
        assertThat(dashboard.widgetConfigurations[0].key).isEqualTo("other-widget")
        assertThat(dashboard.widgetConfigurations[0].dataSourceKey).isEqualTo("other-key")
        assertThat(dashboard.widgetConfigurations[0].dataSourceProperties).isNotNull()
        assertThat(dashboard.widgetConfigurations[0].displayType).isEqualTo("other-type")
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties).isNotNull()
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties.get("subtitle").asText()).isEqualTo("Some other test")
    }

    @Test
    fun `should update dashboard after new changeset s`() {
        whenever(dashboardDeployer.getPath()).thenReturn("classpath*:**/dashboard-update-v*.json")

        changelogDeployer.deployAll()

        val dashboards = dashboardRepository.findAll()
        assertThat(dashboards.size).isEqualTo(1)
        val dashboard = dashboards[0]

        assertThat(dashboard.key).isEqualTo("team-dashboard")
        assertThat(dashboard.title).isEqualTo("Other dashboard")
        assertThat(dashboard.order).isEqualTo(1)
        assertThat(dashboard.createdBy).isEqualTo("auto-deployed")
        assertThat(dashboard.description).isEqualTo("some other description")
        assertThat(dashboard.widgetConfigurations.size).isEqualTo(1)
        assertThat(dashboard.widgetConfigurations[0].title).isEqualTo("Other widget test")
        assertThat(dashboard.widgetConfigurations[0].key).isEqualTo("other-widget")
        assertThat(dashboard.widgetConfigurations[0].dataSourceKey).isEqualTo("other-key")
        assertThat(dashboard.widgetConfigurations[0].dataSourceProperties).isNotNull()
        assertThat(dashboard.widgetConfigurations[0].displayType).isEqualTo("other-type")
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties).isNotNull()
        assertThat(dashboard.widgetConfigurations[0].displayTypeProperties.get("subtitle").asText()).isEqualTo("Some other test")
    }

    @Test
    fun `should add dashboard with new changeset `() {
        whenever(dashboardDeployer.getPath()).thenReturn("classpath*:**/dashboard-add-v*.json")

        changelogDeployer.deployAll()

        val dashboards = dashboardRepository.findAll()
        assertThat(dashboards.size).isEqualTo(2)
    }

    @Test
    fun `should fail deployment when dashboards have the same order`() {
        whenever(dashboardDeployer.getPath()).thenReturn("classpath*:**/dashboard-fail-v*.json")

        val exception = assertThrows<DeploymentFailedException> {
            changelogDeployer.deployAll()
        }

        assertThat(exception.message).isEqualTo("A dashboard with order 1 already exists.")
    }
}
/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.klant.service

import com.ritense.audit.autoconfigure.AuditAutoConfiguration
import com.ritense.connector.autoconfigure.ConnectorAutoConfiguration
import com.ritense.connector.autoconfigure.ConnectorLiquibaseAutoConfiguration
import com.ritense.connector.autoconfigure.ConnectorSecurityAutoConfiguration
import com.ritense.document.autoconfigure.DocumentAutoConfiguration
import com.ritense.document.autoconfigure.DocumentLiquibaseAutoConfiguration
import com.ritense.document.autoconfigure.DocumentRetryAutoConfiguration
import com.ritense.document.autoconfigure.DocumentSecurityAutoConfiguration
import com.ritense.document.autoconfigure.DocumentSnapshotAutoConfiguration
import com.ritense.openzaak.autoconfigure.OpenZaakAutoConfiguration
import com.ritense.openzaak.autoconfigure.OpenZaakLiquibaseAutoConfiguration
import com.ritense.openzaak.autoconfigure.OpenZaakSecurityAutoConfiguration
import com.ritense.openzaak.besluit.BesluitAutoConfiguration
import com.ritense.processdocument.autoconfigure.ProcessDocumentAuditAutoConfiguration
import com.ritense.processdocument.autoconfigure.ProcessDocumentAutoConfiguration
import com.ritense.valtimo.autoconfigure.AccessAndEntitlementAutoConfiguration
import com.ritense.valtimo.autoconfigure.AuthenticationAutoConfiguration
import com.ritense.valtimo.autoconfigure.CamundaAutoConfiguration
import com.ritense.valtimo.autoconfigure.ChoiceFieldAutoConfiguration
import com.ritense.valtimo.autoconfigure.ContextAutoConfiguration
import com.ritense.valtimo.autoconfigure.EmailNotificationSettingsAutoConfiguration
import com.ritense.valtimo.autoconfigure.HttpSecurityAutoConfiguration
import com.ritense.valtimo.autoconfigure.LiquibaseAutoConfiguration
import com.ritense.valtimo.autoconfigure.ProcessDataObjectAutoConfiguration
import com.ritense.valtimo.autoconfigure.ValtimoAutoConfiguration
import com.ritense.valtimo.autoconfigure.ValtimoMethodSecurityAutoConfiguration
import com.ritense.valtimo.autoconfigure.ValtimoMybatisAutoConfiguration
import com.ritense.valtimo.contract.config.LiquibaseRunnerAutoConfiguration
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration

@SpringBootApplication(
    exclude = [
        AccessAndEntitlementAutoConfiguration::class,
        AuditAutoConfiguration::class,
        AuthenticationAutoConfiguration::class,
        BesluitAutoConfiguration::class,
        CamundaAutoConfiguration::class,
        CamundaBpmAutoConfiguration::class,
        ChoiceFieldAutoConfiguration::class,
        ConnectorAutoConfiguration::class,
        ConnectorLiquibaseAutoConfiguration::class,
        ConnectorSecurityAutoConfiguration::class,
        ContextAutoConfiguration::class,
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        DocumentAutoConfiguration::class,
        DocumentSnapshotAutoConfiguration::class,
        DocumentLiquibaseAutoConfiguration::class,
        DocumentSecurityAutoConfiguration::class,
        DocumentRetryAutoConfiguration::class,
        EmailNotificationSettingsAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        HttpSecurityAutoConfiguration::class,
        LiquibaseAutoConfiguration::class,
        LiquibaseRunnerAutoConfiguration::class,
        OpenZaakAutoConfiguration::class,
        OpenZaakLiquibaseAutoConfiguration::class,
        OpenZaakSecurityAutoConfiguration::class,
        ProcessDataObjectAutoConfiguration::class,
        ProcessDocumentAutoConfiguration::class,
        ProcessDocumentAuditAutoConfiguration::class,
        ValtimoAutoConfiguration::class,
        ValtimoMethodSecurityAutoConfiguration::class,
        ValtimoMybatisAutoConfiguration::class,
    ]
)
class KlantTestConfiguration {

    fun main(args: Array<String>) {
        SpringApplication.run(KlantTestConfiguration::class.java, *args)
    }

    @TestConfiguration
    class TestConfig { //Beans extra
    }
}
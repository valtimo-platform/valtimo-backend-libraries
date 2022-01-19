/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.mail.denhaagmailer

import com.ritense.connector.autoconfigure.ConnectorAutoConfiguration
import com.ritense.connector.autoconfigure.ConnectorLiquibaseAutoConfiguration
import com.ritense.connector.autoconfigure.ConnectorSecurityAutoConfiguration
import com.ritense.document.autoconfigure.DocumentAutoConfiguration
import com.ritense.document.autoconfigure.DocumentLiquibaseAutoConfiguration
import com.ritense.document.autoconfigure.DocumentRetryAutoConfiguration
import com.ritense.document.autoconfigure.DocumentSecurityAutoConfiguration
import com.ritense.document.autoconfigure.DocumentSnapshotAutoConfiguration
import com.ritense.mail.autoconfigure.MailAutoConfiguration
import com.ritense.mail.autoconfigure.MailLiquibaseAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        MailAutoConfiguration::class,
        MailLiquibaseAutoConfiguration::class,
        ConnectorAutoConfiguration::class,
        ConnectorLiquibaseAutoConfiguration::class,
        ConnectorSecurityAutoConfiguration::class,
        DocumentAutoConfiguration::class,
        DocumentSnapshotAutoConfiguration::class,
        DocumentLiquibaseAutoConfiguration::class,
        DocumentSecurityAutoConfiguration::class,
        DocumentRetryAutoConfiguration::class
    ]
)
class TestApplication {

    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }
}
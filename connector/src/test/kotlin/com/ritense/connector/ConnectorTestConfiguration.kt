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

package com.ritense.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.impl.DummyDependency
import com.ritense.connector.impl.ObjectApiConnectorType
import com.ritense.connector.impl.ObjectApiProperties
import com.ritense.valtimo.contract.config.LiquibaseRunnerAutoConfiguration
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

@SpringBootApplication(scanBasePackageClasses = [LiquibaseRunnerAutoConfiguration::class])
class ConnectorTestConfiguration {

    fun main(args: Array<String>) {
        SpringApplication.run(ConnectorTestConfiguration::class.java, *args)
    }

    @TestConfiguration
    class TestConfig { //Beans extra

        @Bean
        @Scope(BeanDefinition.SCOPE_PROTOTYPE)
        fun objectApiConnectorType(
            properties: ConnectorProperties,
            dummyDependency: DummyDependency
        ): Connector {
            return ObjectApiConnectorType(properties, dummyDependency)
        }

        @Bean
        fun objectApiProperties(): ConnectorProperties {
            return ObjectApiProperties()
        }

        @Bean
        fun dummyDependency(): DummyDependency {
            return DummyDependency()
        }
    }
}
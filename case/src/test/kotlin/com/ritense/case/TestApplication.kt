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

package com.ritense.case

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.configuration.CaseAutoConfiguration
import com.ritense.valtimo.contract.config.LiquibaseRunnerAutoConfiguration
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.function.Supplier

@SpringBootApplication(
    scanBasePackageClasses = [LiquibaseRunnerAutoConfiguration::class,CaseAutoConfiguration::class],
)
class TestApplication {

    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun hibernateDependencyProcessor(): BeanFactoryPostProcessor? {
            return BeanFactoryPostProcessor { factory: ConfigurableListableBeanFactory ->
                val entityManagerDefinition = factory.getBeanDefinition("entityManagerFactory")
                var entityManagerDependencies = entityManagerDefinition.dependsOn
                entityManagerDependencies = entityManagerDependencies ?: arrayOf()
                val newDependencies = arrayOfNulls<String>(entityManagerDependencies.size + 1)
                System.arraycopy(entityManagerDependencies, 0, newDependencies, 1, entityManagerDependencies.size)
                newDependencies[0] = "hibernateObjectMapperSupplier"
                entityManagerDefinition.setDependsOn(*newDependencies)
            }
        }

        @Bean
        fun hibernateObjectMapperSupplier(): Supplier<ObjectMapper> {
            return HibernateObjectMapperSupplier()
        }

        companion object {
            init {
                System.setProperty(
                    "hibernate.types.jackson.object.mapper",
                    HibernateObjectMapperSupplier::class.java.name
                )
            }
        }
    }
}
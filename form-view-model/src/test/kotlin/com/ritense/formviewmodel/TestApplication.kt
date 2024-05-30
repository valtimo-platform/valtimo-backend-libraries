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

package com.ritense.formviewmodel

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.formviewmodel.autoconfigure.CommandDispatcherAutoConfiguration
import com.ritense.formviewmodel.autoconfigure.FormViewModelAutoConfiguration
import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.formviewmodel.commandhandling.CommandDispatcher
import com.ritense.formviewmodel.commandhandling.CommandHandler
import com.ritense.formviewmodel.commandhandling.ExampleCommandHandler
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.formviewmodel.event.TestSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.valtimo.contract.config.LiquibaseRunnerAutoConfiguration
import com.ritense.valtimo.contract.json.MapperSingleton
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(
    scanBasePackageClasses = [CommandDispatcherAutoConfiguration::class, FormViewModelAutoConfiguration::class]
)
class TestApplication {

    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        @ConditionalOnMissingBean(ObjectMapper::class)
        fun objectMapper(): ObjectMapper {
            return MapperSingleton.get()
        }

        @Bean
        fun testViewModelLoader() = TestViewModelLoader()

        @Bean
        fun testSubmissionHandler(): FormViewModelSubmissionHandler<*> = TestSubmissionHandler()

        @Bean
        fun exampleCommandHandler() = ExampleCommandHandler()

    }
}

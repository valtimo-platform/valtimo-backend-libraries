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

package com.ritense.processlink

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.processlink.domain.AnotherTestProcessLinkMapper
import com.ritense.processlink.domain.AnotherTestSupportedProcessLinksHandler
import com.ritense.processlink.domain.TestProcessLinkMapper
import com.ritense.processlink.domain.TestSupportedProcessLinksHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@SpringBootApplication
class TestApplication {

    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun testProcessLinkMapper(objectMapper: ObjectMapper) = TestProcessLinkMapper(objectMapper)

        @Bean
        fun anotherTestProcessLinkMapper(objectMapper: ObjectMapper) = AnotherTestProcessLinkMapper(objectMapper)


        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        fun testSupportedProcessLinksHandler() = TestSupportedProcessLinksHandler()

        @Bean
        @Order(Ordered.LOWEST_PRECEDENCE)
        fun anotherTestSupportedProcessLinksHandler() = AnotherTestSupportedProcessLinksHandler()

    }
}

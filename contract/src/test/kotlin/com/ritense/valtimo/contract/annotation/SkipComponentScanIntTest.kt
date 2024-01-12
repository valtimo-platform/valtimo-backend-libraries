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

package com.ritense.valtimo.contract.annotation

import com.ritense.valtimo.contract.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
class SkipComponentScanIntTest {

    @Nested
    inner class WithoutBeanConfig @Autowired constructor(
        private val applicationContext: ApplicationContext
    ) : BaseIntegrationTest() {
        @Test
        fun `should create one ComponentScannedComponent`() {
            val beans = applicationContext.getBeansOfType(ComponentScannedComponent::class.java)
            assertThat(beans).hasSize(1)
            assertThat(beans.keys.single()).isEqualTo("componentScannedComponent")
        }

        @Test
        fun `should not create SkippedComponentScanComponent bean`() {
            val beans = applicationContext.getBeansOfType(SkippedComponentScanComponent::class.java)
            assertThat(beans).isEmpty()
        }
    }

    @Nested
    @Import(TestConfig::class)
    inner class WithBeanConfig @Autowired constructor(
        private val applicationContext: ApplicationContext
    ) : BaseIntegrationTest() {

        @Test
        fun `should create two ComponentScannedComponent beans`() {
            val beans = applicationContext.getBeansOfType(ComponentScannedComponent::class.java)
            assertThat(beans).hasSize(2)
            assertThat(beans.keys).containsExactlyInAnyOrder(
                "componentScannedComponent",
                "componentScannedComponentByConfig"
            )
        }

        @Test
        fun `should only create SkippedComponentScanComponent bean with name skippedComponentScanComponentByConfig`() {
            val beans = applicationContext.getBeansOfType(SkippedComponentScanComponent::class.java)
            assertThat(beans.keys).containsOnly("skippedComponentScanComponentByConfig")
        }
    }

}

@Component
class ComponentScannedComponent

@Component
@SkipComponentScan
class SkippedComponentScanComponent

@TestConfiguration
class TestConfig {

    @Bean
    fun componentScannedComponentByConfig() = ComponentScannedComponent()

    @Bean
    fun skippedComponentScanComponentByConfig() = SkippedComponentScanComponent()
}
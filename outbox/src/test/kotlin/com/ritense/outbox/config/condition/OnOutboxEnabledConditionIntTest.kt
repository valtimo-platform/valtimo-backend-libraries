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

package com.ritense.outbox.config.condition

import com.ritense.outbox.NoopOutboxService
import com.ritense.outbox.ValtimoOutboxService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@Tag("integration")
class OnOutboxEnabledConditionIntTest {

    @Nested
    @SpringBootTest(properties = ["${OnOutboxEnabledCondition.PROPERTY_NAME}=true"])
    inner class Enabled @Autowired constructor(
        private val context: ApplicationContext
    ) {
        @Test
        fun `Should create an DefaultOutboxService bean`() {
            val bean = context.getBean(ValtimoOutboxService::class.java)
            Assertions.assertThat(bean).isNotNull
        }
    }

    @Nested
    @SpringBootTest(properties = ["${OnOutboxEnabledCondition.PROPERTY_NAME}=false"])
    inner class Disabled @Autowired constructor(
        private val context: ApplicationContext
    ) {
        @Test
        fun `Should create NoopOutboxService bean`() {
            val bean = context.getBean(NoopOutboxService::class.java)
            Assertions.assertThat(bean).isNotNull
        }
    }

    @Nested
    @SpringBootTest
    inner class Default @Autowired constructor(
        private val context: ApplicationContext
    ) {
        @Test
        fun `Should create NoopOutboxService bean`() {
            val bean = context.getBean(NoopOutboxService::class.java)
            Assertions.assertThat(bean).isNotNull
        }
    }
}
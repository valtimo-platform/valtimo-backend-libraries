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

package com.ritense.logging

import com.ritense.logging.testimpl.LogResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LoggableResourceIT : BaseIntegrationTest() {

    @AfterEach
    fun afterEach() {
        LogResource.listAppender.list.clear()
    }

    @Test
    fun `should log resource`() {
        logResourceBean.someMethod(LogResource(), "123")
        val messages = LogResource.listAppender.list
        assertThat(messages).hasSize(1)
        assertThat(messages[0].mdcPropertyMap[String::class.java.canonicalName]).isEqualTo("123")
    }

    @Test
    fun `should log resource with resourceTypeName`() {
        val loggableResource = LogResource()
        logResourceBean.someMethodWithResourceTypeName(loggableResource, "123")

        val messages = LogResource.listAppender.list
        assertThat(messages).hasSize(1)
        assertThat(messages[0].mdcPropertyMap["kotlin.String"]).isEqualTo("123")
    }

    @Test
    fun `should throw exception with no loggable resource type`() {
        assertThrows<IllegalStateException> {
            logResourceBean.someMethodWithNoLoggableResourceType(LogResource(), "123")
        }
    }
}
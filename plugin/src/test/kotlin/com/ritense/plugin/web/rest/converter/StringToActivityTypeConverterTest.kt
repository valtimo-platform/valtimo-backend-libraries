/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.plugin.web.rest.converter

import com.ritense.plugin.domain.ActivityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StringToActivityTypeConverterTest {

    @Test
    fun `should return SERVICE_TASK`() {
        assertEquals(ActivityType.SERVICE_TASK_START, ActivityType.fromValue("bpmn:ServiceTask:start"))
    }

    @Test
    fun `should return error on invalid value`() {
        assertThrows<IllegalStateException> { ActivityType.fromValue("invalidValue") }
    }
}
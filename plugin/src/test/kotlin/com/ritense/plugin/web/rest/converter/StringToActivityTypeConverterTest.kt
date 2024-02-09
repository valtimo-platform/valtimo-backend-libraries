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

package com.ritense.plugin.web.rest.converter

import com.ritense.processlink.domain.ActivityTypeWithEventName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StringToActivityTypeConverterTest {

    @Test
    fun `should return SERVICE_TASK_START`() {
        assertEquals(ActivityTypeWithEventName.SERVICE_TASK_START, ActivityTypeWithEventName.fromValue("bpmn:ServiceTask:start"))
    }

    @Test
    fun `should return error on invalid value`() {
        assertThrows<IllegalStateException> { ActivityTypeWithEventName.fromValue("invalidValue") }
    }

    @Test
    fun `should NOT map activity types that can't be listened to by @CamundaSelector`() {
        assertThrows<IllegalStateException> { ActivityTypeWithEventName.fromValue("bpmn:EndEvent:start") }
        assertThrows<IllegalStateException> { ActivityTypeWithEventName.fromValue("bpmn:Lane:start") }
        assertThrows<IllegalStateException> { ActivityTypeWithEventName.fromValue("bpmn:Participant:start") }
        assertThrows<IllegalStateException> { ActivityTypeWithEventName.fromValue("bpmn:SequenceFlow:start") }
        assertThrows<IllegalStateException> { ActivityTypeWithEventName.fromValue("bpmn:TextAnnotation:start") }
    }
}
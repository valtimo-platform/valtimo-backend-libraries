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

package com.ritense.processlink.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.processlink.domain.CustomProcessLink.Companion.PROCESS_LINK_TYPE_TEST
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.util.UUID


class CustomProcessLinkCreateRequestDtoTest {

    private val mapper = jacksonObjectMapper().apply {
        this.registerSubtypes(CustomProcessLinkCreateRequestDto::class.java, CustomProcessLinkUpdateRequestDto::class.java)
    }

    @Test
    fun `should deserialise correctly with processLinkType`() {
        val value: ProcessLinkCreateRequestDto = mapper.readValue("""
            {
                "processDefinitionId": "process-definition:1",
                "activityId": "serviceTask1",
                "activityType": "${ActivityTypeWithEventName.SERVICE_TASK_START.value}",
                "processLinkType": "$PROCESS_LINK_TYPE_TEST",
                "someValue": "test"
            }
        """.trimIndent())

        assertThat(value, instanceOf(CustomProcessLinkCreateRequestDto::class.java))
        value as CustomProcessLinkCreateRequestDto
        assertThat(value.processLinkType, equalTo(PROCESS_LINK_TYPE_TEST))
        assertThat(value.someValue, equalTo("test"))
    }

    @Test
    fun `should deserialise correctly without processLinkType`() {
        val value: ProcessLinkCreateRequestDto = mapper.readValue("""
            {
                "processDefinitionId": "process-definition:1",
                "activityId": "serviceTask1",
                "activityType": "${ActivityTypeWithEventName.SERVICE_TASK_START.value}",
                "someValue": "test"
            }
        """.trimIndent())

        assertThat(value, instanceOf(CustomProcessLinkCreateRequestDto::class.java))
        value as CustomProcessLinkCreateRequestDto
        assertThat(value.processLinkType, equalTo(PROCESS_LINK_TYPE_TEST))
        assertThat(value.someValue, equalTo("test"))
    }

    @Test
    fun `should serialize correctly`() {
        val id = UUID.randomUUID()
        val value = CustomProcessLinkCreateRequestDto(
            processLinkId = id,
            processDefinitionId = "process-definition:1",
            activityId = "serviceTask1",
            activityType = ActivityTypeWithEventName.SERVICE_TASK_START,
            someValue = "test"
        )
        val json = mapper.writeValueAsString(value)

        JSONAssert.assertEquals("""
            {
              "processLinkId": $id,
              "processDefinitionId": "${value.processDefinitionId}",
              "activityId": "${value.activityId}",
              "activityType": "${value.activityType.value}",
              "processLinkType": "$PROCESS_LINK_TYPE_TEST",
              "someValue": "test"
            }
        """.trimIndent(), json, JSONCompareMode.NON_EXTENSIBLE)
    }
}

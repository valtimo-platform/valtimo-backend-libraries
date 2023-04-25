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

package com.ritense.authorization.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class FieldPermissionConditionTest {

    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setup() {
        mapper = jacksonObjectMapper().apply {
            this.registerSubtypes(FieldPermissionCondition::class.java)
        }
    }

    @Test
    fun `should serialize correctly`() {
        val condition = FieldPermissionCondition(field = "test", value = "my-value")

        val json = mapper.writeValueAsString(condition)
        JSONAssert.assertEquals("""
            {
              "type": "${condition.type}",
              "field": "${condition.field}",
              "value": "${condition.value}"
            }
        """.trimIndent(), json, JSONCompareMode.NON_EXTENSIBLE)
    }

    @Test
    fun `should deserialise correctly with processLinkType`() {
        val value: PermissionCondition = mapper.readValue("""
            {
              "type": "FIELD",
              "field": "test-field",
              "value": "test-value"
            }
        """.trimIndent())

        MatcherAssert.assertThat(value, Matchers.instanceOf(FieldPermissionCondition::class.java))
        value as FieldPermissionCondition
        MatcherAssert.assertThat(value.type, Matchers.equalTo(PermissionConditionType.FIELD))
        MatcherAssert.assertThat(value.field, Matchers.equalTo("test-field"))
        MatcherAssert.assertThat(value.value, Matchers.equalTo("test-value"))
    }
}
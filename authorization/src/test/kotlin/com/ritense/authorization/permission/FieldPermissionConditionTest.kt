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

package com.ritense.authorization.permission

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.testimpl.TestChildEntity
import com.ritense.authorization.testimpl.TestEntity
import kotlin.test.assertEquals
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class FieldPermissionConditionTest {
    lateinit var mapper: ObjectMapper
    lateinit var entity: TestEntity
    lateinit var conditionTemplate: FieldPermissionCondition

    @BeforeEach
    fun setup() {
        mapper = jacksonObjectMapper().apply {
            this.registerSubtypes(FieldPermissionCondition::class.java)
        }
        //TODO: The entity or child objects can't be a Map, don't we want to support this?
        entity = TestEntity(
            TestChildEntity(true)
        )

        conditionTemplate = FieldPermissionCondition("child.property", "true")
    }

    @Test
    fun `should find property and pass the condition`() {
        val condition = conditionTemplate
        val result = condition.isValid(entity)
        assertEquals(true, result)
    }

    @Test
    fun `should find property and fail the condition`() {
        val condition = conditionTemplate.copy(value = "false")
        val result = condition.isValid(entity)
        assertEquals(false, result)
    }

    @Test
    fun `should not find property and throw exception`() {
        val condition = conditionTemplate.copy(field = "child.non-existent")
        assertThrows<NoSuchFieldException> {
            condition.isValid(entity)
        }
    }

    @Test
    fun `should serialize to JSON`() {
        val condition = conditionTemplate

        val json = mapper.writeValueAsString(condition)
        JSONAssert.assertEquals(
            """
            {
              "type": "${condition.type.value}",
              "field": "${condition.field}",
              "value": "${condition.value}"
            }
        """.trimIndent(), json, JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `should deserialize from JSON`() {
        val result: PermissionCondition = mapper.readValue(
            """
            {
              "type": "field",
              "field": "test-field",
              "value": "test-value"
            }
        """.trimIndent()
        )

        MatcherAssert.assertThat(result, Matchers.instanceOf(FieldPermissionCondition::class.java))
        result as FieldPermissionCondition
        MatcherAssert.assertThat(result.type, Matchers.equalTo(PermissionConditionType.FIELD))
        MatcherAssert.assertThat(result.field, Matchers.equalTo("test-field"))
        MatcherAssert.assertThat(result.value, Matchers.equalTo("test-value"))
    }
}
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
import com.ritense.authorization.permission.PermissionConditionOperator.EQUAL_TO
import com.ritense.authorization.permission.PermissionConditionOperator.GREATER_THAN
import com.ritense.authorization.permission.PermissionConditionOperator.LESS_THAN
import com.ritense.authorization.permission.PermissionConditionOperator.NOT_EQUAL_TO
import com.ritense.authorization.testimpl.TestChildEntity
import com.ritense.authorization.testimpl.TestEntity
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FieldPermissionConditionTest {
    lateinit var mapper: ObjectMapper
    lateinit var entity: TestEntity
    lateinit var conditionTemplate: FieldPermissionCondition<Int>

    @BeforeEach
    fun setup() {
        mapper = jacksonObjectMapper().apply {
            this.registerSubtypes(FieldPermissionCondition::class.java)
        }
        //TODO: The entity or child objects can't be a Map, don't we want to support this?
        entity = TestEntity(
            TestChildEntity(100)
        )

        conditionTemplate = FieldPermissionCondition("child.property", EQUAL_TO, 100)
    }

    @Test
    fun `should pass validation with NOT_EQUAL_TO comparator`() {
        val condition = FieldPermissionCondition("child.property", NOT_EQUAL_TO, 99)
        val result = condition.isValid(entity)
        assertTrue(result)
    }

    @Test
    fun `should pass validation when the property value is equal`() {
        val condition = conditionTemplate
        val result = condition.isValid(entity)
        assertTrue(result)
    }

    @Test
    fun `should pass validation with GREATER_THAN comparator`() {
        val condition = FieldPermissionCondition("child.property", GREATER_THAN, 99)
        val result = condition.isValid(entity)
        assertTrue(result)
    }

    @Test
    fun `should pass validation with LESS_THAN comparator`() {
        val condition = FieldPermissionCondition("child.property", LESS_THAN, 101)
        val result = condition.isValid(entity)
        assertTrue(result)
    }

    @Test
    fun `should fail validation when the property value is not equal`() {
        val condition = conditionTemplate.copy(value = 99)
        val result = condition.isValid(entity)
        assertFalse(result)
    }

    @Test
    fun `should fail validation when the property value type is different`() {
        val condition = FieldPermissionCondition("child.property", EQUAL_TO, "test")
        val result = condition.isValid(entity)
        assertFalse(result)
    }

    @Test
    fun `should pass validation with property LocalDateTime`() {
        val entity = TestEntity(
            TestChildEntity(LocalDate.parse("2000-01-01"))
        )
        val condition = FieldPermissionCondition("child.property", LESS_THAN, LocalDate.parse("2023-05-25"))
        val result = condition.isValid(entity)
        assertTrue(result)
    }

    @Test
    fun `should fail validation when property value is null`() {
        val entity = TestEntity(
            TestChildEntity(null)
        )
        val condition = conditionTemplate.copy(value = 100)
        val result = condition.isValid(entity)
        assertFalse(result)
    }

    @Test
    fun `should fail validation when property value is null and condition value is String('null')`() {
        // Checking a null by passing a String("null") is strange behaviour,
        // because it would also pass when the property value is not null but "null".
        val entity = TestEntity(
            TestChildEntity(null)
        )
        val condition = FieldPermissionCondition("child.property", EQUAL_TO, "null")
        val result = condition.isValid(entity)
        assertFalse(result)
    }

    @Test
    fun `should pass validation when property value is null and condition value is null`() {
        // Checking a null by passing a String("null") is strange behaviour,
        // because it would also pass when the property value is not null but "null".
        val entity = TestEntity(
            TestChildEntity(null)
        )
        val condition = conditionTemplate.copy(value = null)
        val result = condition.isValid(entity)
        assertTrue(result)
    }

    @Test
    fun `should throw NoSuchFieldException when the property field cannot be found`() {
        val condition = conditionTemplate.copy(field = "child.non-existent")
        assertThrows<NoSuchFieldException> {
            condition.isValid(entity)
        }
    }

    @Test
    fun `should throw NullPointerException when a parent in the path is null`() {
        val entity = TestEntity(null)
        val condition = conditionTemplate
        assertThrows<NullPointerException> {
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
              "type": "${PermissionConditionType.FIELD.value}",
              "field": "child.property",
              "operator": "==",
              "value": 100
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
              "operator": ">",
              "value": true
            }
        """.trimIndent()
        )

        MatcherAssert.assertThat(result, Matchers.instanceOf(FieldPermissionCondition::class.java))
        result as FieldPermissionCondition<*>
        MatcherAssert.assertThat(result.type, Matchers.equalTo(PermissionConditionType.FIELD))
        MatcherAssert.assertThat(result.field, Matchers.equalTo("test-field"))
        MatcherAssert.assertThat(result.operator, Matchers.equalTo(GREATER_THAN))
        MatcherAssert.assertThat(result.value, Matchers.equalTo(true))
    }
}
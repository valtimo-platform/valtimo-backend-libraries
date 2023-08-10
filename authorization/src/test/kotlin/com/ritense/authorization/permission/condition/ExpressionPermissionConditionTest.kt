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

package com.ritense.authorization.permission.condition

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.permission.condition.PermissionExpressionOperator.EQUAL_TO
import com.ritense.authorization.testimpl.TestChildEntity
import com.ritense.authorization.testimpl.TestEntity
import kotlin.test.assertEquals
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class ExpressionPermissionConditionTest {

    lateinit var mapper: ObjectMapper
    lateinit var entity: TestEntity
    lateinit var conditionTemplate: ExpressionPermissionCondition<String>

    @BeforeEach
    fun setup() {
        mapper = jacksonObjectMapper().apply {
            this.registerSubtypes(ExpressionPermissionCondition::class.java)
        }
        //TODO: The entity or child objects can't be a Map, don't we want to support this?
        entity = TestEntity(
            TestChildEntity("""
            {
                "value": "myValue"
            }
        """.trimIndent()))

        conditionTemplate = createExpressionCondition("myValue", String::class.java)
    }

    private fun <T: Comparable<T>>createExpressionCondition(value: T, clazz: Class<T>) = ExpressionPermissionCondition(
        field = "child.property",
        path = "value",
        operator = EQUAL_TO,
        value = value,
        clazz = clazz
    )

    @Test
    fun `should pass validation when the property value is equal`() {
        val condition = conditionTemplate

        val result = condition.isValid(entity)
        assertEquals(true, result)
    }

    @Test
    fun `should fail validation when the property value is not equal`() {
        val condition = conditionTemplate.copy(value = "notMyValue")

        val result = condition.isValid(entity)
        assertEquals(false, result)
    }

    @Test
    fun `should fail validation when the property type is not equal`() {
        val condition = createExpressionCondition(true, Boolean::class.java)

        val result = condition.isValid(entity)
        assertEquals(false, result)
    }

    @Test
    fun `should throw an exception when entity property is not found and condition value is not null`() {
        val condition = conditionTemplate.copy(field = "x")
        assertThrows<NoSuchFieldException> {
            condition.isValid(entity)
        }
    }

    @Test
    fun `should pass validation when entity property value is null and condition value is null`() {
        val entity = TestEntity(TestChildEntity(null))
        val condition = conditionTemplate.copy(value = null)
        val result = condition.isValid(entity)
        assertEquals(true, result)
    }

    @Test
    fun `should fail validation when json property is not found and condition value is not null`() {
        val condition = conditionTemplate.copy(path = "y")

        val result = condition.isValid(entity)
        assertEquals(false, result)
    }

    @Test
    fun `should pass validation when json property is not found and condition value is null`() {
        val condition = conditionTemplate.copy(path = "y", value = null)

        val result = condition.isValid(entity)
        assertEquals(true, result)
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
              "path": "${condition.path}",
              "operator": "${condition.operator.asText}",
              "value": "${condition.value}",
              "clazz": "java.lang.String"
            }
        """.trimIndent(), json, JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `should deserialize from JSON`() {
        val result: PermissionCondition = mapper.readValue(
            """
                {
                    "type":"expression",
                    "field":"child.property",
                    "path":"value",
                    "operator":"==",
                    "value":"myValue",
                    "clazz":"java.lang.String"
                }
            """.trimIndent()
        )

        MatcherAssert.assertThat(result, Matchers.equalTo(conditionTemplate))
    }
}
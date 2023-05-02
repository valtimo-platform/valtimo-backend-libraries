package com.ritense.authorization.permission

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.permission.PermissionExpressionOperator.EQUAL_TO
import com.ritense.authorization.testimpl.TestChildEntity
import com.ritense.authorization.testimpl.TestEntity
import kotlin.test.Ignore
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
                "stringProperty": "myValue"
            }
        """.trimIndent()))

        conditionTemplate = ExpressionPermissionCondition(
            field = "child.property",
            path = "stringProperty",
            operator = EQUAL_TO,
            value = "myValue",
            clazz = String::class.java)
    }

    @Test
    fun `should find property and pass expression evaluation`() {
        val condition = conditionTemplate

        val result = condition.isValid(entity)
        assertEquals(true, result)
    }

    @Test
    fun `should find property and fail expression evaluation`() {
        val condition = conditionTemplate.copy(value = "notMyValue")

        val result = condition.isValid(entity)
        assertEquals(false, result)
    }

    @Test
    fun `should not find entity property and throw exception`() {
        val condition = conditionTemplate.copy(field = "x")
        assertThrows<NoSuchFieldException> {
            condition.isValid(entity)
        }
    }

    @Test
    fun `should not find json property and fail evaluation`() {
        val condition = conditionTemplate.copy(path = "y")

        val result = condition.isValid(entity)
        assertEquals(false, result)
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
                    "path":"stringProperty",
                    "operator":"==",
                    "value":"myValue",
                    "clazz":"java.lang.String"
                }
            """.trimIndent()
        )

        MatcherAssert.assertThat(result, Matchers.equalTo(conditionTemplate))
    }
}
package com.ritense.document.dashboard

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QueryConditionTest {

    @Test
    fun `should deserialize with typed value`() {
        val value = jacksonObjectMapper().readValue(
            """
            {
                "queryPath": "/xyz",
                "queryOperator": "==",
                "queryValue": 69
            }
        """.trimIndent(), QueryCondition::class.java
        )

        assertThat(value).isNotNull
        assertThat(value.queryValue).isInstanceOf(Number::class.java)
    }

}
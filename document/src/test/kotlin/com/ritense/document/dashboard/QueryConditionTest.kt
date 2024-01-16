package com.ritense.document.dashboard

import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QueryConditionTest {

    @Test
    fun `should deserialize with number value`() {
        val value = MapperSingleton.get().readValue(
            """
            {
                "queryPath": "/xyz",
                "queryOperator": "==",
                "queryValue": 69
            }
        """.trimIndent(), QueryCondition::class.java
        )

        assertThat(value).isNotNull
        assertThat(value.queryValue).isEqualTo(69)
        assertThat(value.queryValue).isNotEqualTo("69")
    }

    @Test
    fun `should deserialize with string value`() {
        val value = MapperSingleton.get().readValue(
            """
            {
                "queryPath": "/xyz",
                "queryOperator": "==",
                "queryValue": "69"
            }
        """.trimIndent(), QueryCondition::class.java
        )

        assertThat(value).isNotNull
        assertThat(value.queryValue).isEqualTo("69")
        assertThat(value.queryValue).isNotEqualTo(69)
    }

}
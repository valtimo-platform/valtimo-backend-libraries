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
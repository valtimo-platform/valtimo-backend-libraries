package com.ritense.valtimo.contract.json

import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class JsonMergerTest {

    @Test
    fun `should do a deep merge of objects`() {
        val mapper = Mapper.INSTANCE.get()
        val first = mapper.readTree(
            """
            {
                "root": {
                    "x": 1,
                    "arr": [
                        1
                    ],
                    "existing": 1
                }
            }
            """.trimIndent()
        )
        val second = mapper.readTree(
            """
            {
                "root": {
                    "y": 2,
                    "arr": [
                        2
                    ],
                    "existing": 2
                }
            }
            """.trimIndent()
        )

        val merged = JsonMerger.merge(first, second)

        val json = mapper.writeValueAsString(merged)
        assertThat(json, hasJsonPath("""${'$'}.root.x""", equalTo((1))))
        assertThat(json, hasJsonPath("""${'$'}.root.y""", equalTo(2)))
        assertThat(json, hasJsonPath("""${'$'}.root.arr[0]""", equalTo(1)))
        assertThat(json, hasJsonPath("""${'$'}.root.arr[1]""", equalTo(2)))
        assertThat(json, hasJsonPath("""${'$'}.root.existing""", equalTo(2)))
    }
}
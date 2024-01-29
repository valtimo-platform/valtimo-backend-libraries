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

package com.ritense.script.service

import com.ritense.script.domain.Script
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

internal class ScriptServiceIntTest {

    lateinit var scriptService: ScriptService
    lateinit var valtimoScriptPrefiller: ValtimoScriptPrefiller

    @BeforeEach
    fun beforeEach() {
        scriptService = mock()
        valtimoScriptPrefiller = ValtimoScriptPrefiller(scriptService)
    }

    @Test
    fun `should replace placeholder with script`() {
        whenever(scriptService.getScript("my-script")).thenReturn(Script("my-script", "var1 + var2"))

        val result = valtimoScriptPrefiller.prefillScript(
            """
            var1 = 1;
            var2 = 2;
            result = {{my-script}};
        """.trimIndent()
        )

        assertThat(result).isEqualTo(
            """
            var1 = 1;
            var2 = 2;
            result = var1 + var2;
        """.trimIndent()
        )
    }

}
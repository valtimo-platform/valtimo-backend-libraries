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

package com.ritense.template.service

import com.ritense.template.domain.ValtimoTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

internal class ValtimoTemplateServiceIntTest {

    lateinit var templateService: TemplateService
    lateinit var valtimoTemplatePrefiller: ValtimoTemplatePrefiller

    @BeforeEach
    fun beforeEach() {
        templateService = mock()
        valtimoTemplatePrefiller = ValtimoTemplatePrefiller(templateService)
    }

    @Test
    fun `should replace placeholder with template`() {
        whenever(templateService.getTemplate("my-template")).thenReturn(ValtimoTemplate("my-template", "var1 + var2"))

        val result = valtimoTemplatePrefiller.prefillTemplate(
            """
            var1 = 1;
            var2 = 2;
            result = {{my-template}};
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
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

import com.ritense.template.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class ValtimoTemplateDeploymentServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var templateService: TemplateService

    @Test
    fun `should deploy template from resource folder`() {

        val template = templateService.getTemplate("test-template")

        assertThat(template.key).isEqualTo("test-template")
        assertThat(template.content).isEqualTo("var1 + var2 + 100;")
    }

}
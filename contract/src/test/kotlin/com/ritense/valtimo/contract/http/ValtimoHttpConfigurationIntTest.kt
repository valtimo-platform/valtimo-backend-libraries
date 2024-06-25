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

package com.ritense.valtimo.contract.http

import com.ritense.valtimo.contract.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals


class ValtimoHttpConfigurationIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var valtimoHttpRestTemplatesConfigurationProperties: ValtimoHttpRestTemplateConfigurationProperties

    @Autowired
    lateinit var valtimoHttpWebClientConfigurationProperties: ValtimoHttpWebClientConfigurationProperties

    @Test
    fun `should set default values`() {

        assertEquals(30, valtimoHttpRestTemplatesConfigurationProperties.connectionTimeout)
        assertEquals(40, valtimoHttpRestTemplatesConfigurationProperties.readTimeout)
        assertEquals(50, valtimoHttpWebClientConfigurationProperties.connectionTimeout)
        assertEquals(60, valtimoHttpWebClientConfigurationProperties.readTimeout)
    }
}
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

package com.ritense.valtimo.contract.config

import com.ritense.valtimo.contract.BaseIntegrationTest
import com.ritense.valtimo.contract.http.ValtimoHttpRestTemplatesConfigurationProperties
import com.ritense.valtimo.contract.http.ValtimoHttpWebClientConfigurationProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals


class ValtimoHttpConfigurationIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var valtimoHttpRestTemplatesConfigurationProperties: ValtimoHttpRestTemplatesConfigurationProperties

    @Autowired
    lateinit var valtimoHttpWebClientConfigurationProperties: ValtimoHttpWebClientConfigurationProperties

    @Test
    fun `should set default values`() {

        assertEquals(10, valtimoHttpRestTemplatesConfigurationProperties.connectionTimeout)
        assertEquals(10, valtimoHttpRestTemplatesConfigurationProperties.readTimeout)
        assertEquals(10, valtimoHttpWebClientConfigurationProperties.connectionTimeout)
    }
}
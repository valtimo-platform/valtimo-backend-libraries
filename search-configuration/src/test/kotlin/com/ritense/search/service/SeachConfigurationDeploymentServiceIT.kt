/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.search.service

import com.ritense.search.BaseIntegrationTest
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.MatchType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class SearchConfigurationDeploymentServiceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var searchConfigurationService: SearchConfigurationService

    @Test
    fun `should deploy search configuration from resources`() {

        val configuration = searchConfigurationService.getConfigurationByName("profile")!!

        assertThat(configuration.name).isEqualTo("profile")
        assertThat(configuration.searchFields).hasSize(1)
        assertThat(configuration.searchFields.first().key).isEqualTo("lastname")
        assertThat(configuration.searchFields.first().path).isEqualTo("/lastname")
        assertThat(configuration.searchFields.first().dataType).isEqualTo(DataType.TEXT)
        assertThat(configuration.searchFields.first().fieldType).isEqualTo(FieldType.SINGLE)
        assertThat(configuration.searchFields.first().matchType).isEqualTo(MatchType.LIKE)
    }
}

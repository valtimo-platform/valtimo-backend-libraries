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

package com.ritense.search.autodeployment

import com.ritense.search.BaseIntegrationTest
import com.ritense.search.service.SearchListColumnService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional


@Transactional
internal class SearchListColumnDefinitionDeploymentServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var searchListColumnService: SearchListColumnService

    @Test
    fun getById() {
        val searchListColumn = searchListColumnService.findByOwnerId("5f35c270-21f4-4e99-a8a1-6c4f9d5a6c5c")
        assertThat(searchListColumn).isNotEmpty
        assertThat(searchListColumn?.first()?.title).isEqualTo("My search list column")
    }
}
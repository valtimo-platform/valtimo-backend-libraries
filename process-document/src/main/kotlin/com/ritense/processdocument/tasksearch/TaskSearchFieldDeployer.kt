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

package com.ritense.processdocument.tasksearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.processdocument.service.SEARCH_FIELD_OWNER_TYPE
import com.ritense.search.deployment.SearchFieldDeployer
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.valtimo.changelog.service.ChangelogService

class TaskSearchFieldDeployer(
    objectMapper: ObjectMapper,
    changelogService: ChangelogService,
    repository: SearchFieldV2Repository,
    searchFieldService: SearchFieldV2Service,
    clearTables: Boolean,
): SearchFieldDeployer(objectMapper, changelogService, repository, searchFieldService, clearTables) {
    override fun getPath(): String {
        return "classpath*:**/*.task-search-field.json"
    }

    override fun ownerTypeKey(): String {
        return SEARCH_FIELD_OWNER_TYPE
    }

    override fun changeSetKey(): String {
        return "task-search-field"
    }
}
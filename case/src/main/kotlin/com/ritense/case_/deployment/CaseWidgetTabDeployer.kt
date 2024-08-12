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

package com.ritense.case_.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.case.domain.CaseTabId
import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import com.ritense.case_.widget.CaseWidgetMapper
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.validation.check
import jakarta.validation.Validator

class CaseWidgetTabDeployer(
    private val objectMapper: ObjectMapper,
    private val caseWidgetTabRepository: CaseWidgetTabRepository,
    private val caseWidgetMappers: List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean,
    private val validator: Validator
) : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.case-widget-tab.json"

    override fun before() {
        if (clearTables) {
            caseWidgetTabRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<CaseWidgetTabChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.tabs,
                key = KEY,
                deploy = { deploy(changeset.tabs) }
            )
        )
    }

    fun deploy(tabs: List<CaseWidgetTabDto>) {
        validator.check(tabs)

        val toSave = tabs.map { tab ->
            CaseWidgetTab(
                CaseTabId(
                    caseDefinitionName = tab.caseDefinitionName,
                    key = tab.key
                ),
                widgets = tab.widgets.mapIndexed { index, widgetDto ->
                    caseWidgetMappers.first { mapper ->
                        mapper.supportedDtoType().isAssignableFrom(widgetDto::class.java)
                    }.toEntity(widgetDto, index)
                }
            )
        }

        caseWidgetTabRepository.saveAll(toSave)
    }

    companion object {
        const val KEY = "case-widget-tab"
    }
}
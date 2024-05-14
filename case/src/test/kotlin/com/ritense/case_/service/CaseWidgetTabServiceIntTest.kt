package com.ritense.case_.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabId
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.repository.CaseWidgetTabRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseWidgetTabServiceIntTest @Autowired constructor(
    private val caseTabService: CaseTabService,
    private val caseWidgetTabRepository: CaseWidgetTabRepository,
    private val caseWidgetTabService: CaseWidgetTabService
) : BaseIntegrationTest() {

    @Test
    fun `should create widget tab when tab of type widgets is created`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        val tabId = CaseTabId(caseDefinitionName, tabKey)

        assertThat(caseWidgetTabRepository.existsById(tabId)).isFalse()

        runWithoutAuthorization {
            caseTabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
        }

        assertThat(caseWidgetTabRepository.existsById(tabId)).isTrue()
    }

    @Test
    fun `should get widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        runWithoutAuthorization {
            caseTabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
        }

        val widgetTab = caseWidgetTabService.getWidgetTab(caseDefinitionName, tabKey)
        assertThat(widgetTab).isNotNull
    }

}
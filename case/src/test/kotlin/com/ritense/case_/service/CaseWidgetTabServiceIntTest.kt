package com.ritense.case_.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabId
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.web.rest.dto.TestCaseWidgetTabWidgetDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
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

    @Test
    fun `should remove widget tab when case tab is removed`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"


        runWithoutAuthorization {
            caseTabService.createCaseTab(caseDefinitionName, CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-"))
        }

        val tabId = CaseTabId(caseDefinitionName, tabKey)
        assertThat(caseWidgetTabRepository.existsById(tabId)).isTrue()
        runWithoutAuthorization {
            caseTabService.deleteCaseTab(caseDefinitionName, tabKey)
        }
        assertThat(caseWidgetTabRepository.existsById(tabId)).isFalse()
    }

    @Test
    fun `should add widgets to widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        runWithoutAuthorization {
            caseTabService.createCaseTab(
                caseDefinitionName,
                CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-")
            )

            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey,
                    widgets = listOf(
                        TestCaseWidgetTabWidgetDto("widget-1", "Widget 1", 0, false),
                        TestCaseWidgetTabWidgetDto("widget-2", "Widget 2", 1, true)
                    )
                )
            )
        }

        val widgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(widgetTab).isNotNull

        assertThat(widgetTab!!.widgets).hasSize(2)
        assertThat(widgetTab.widgets[0].key).isEqualTo("widget-1")
        assertThat(widgetTab.widgets[1].key).isEqualTo("widget-2")
        assertThat(widgetTab.widgets[0].title).isEqualTo("Widget 1")
        assertThat(widgetTab.widgets[1].title).isEqualTo("Widget 2")
        assertThat(widgetTab.widgets[0].width).isEqualTo(0)
        assertThat(widgetTab.widgets[1].width).isEqualTo(1)
        assertThat(widgetTab.widgets[0].highContrast).isFalse()
        assertThat(widgetTab.widgets[1].highContrast).isTrue()
        assertThat(widgetTab.widgets[0].order).isEqualTo(0)
        assertThat(widgetTab.widgets[1].order).isEqualTo(1)
    }

    @Test
    fun `should remove widgets from widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        runWithoutAuthorization {
            caseTabService.createCaseTab(
                caseDefinitionName,
                CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-")
            )

            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey,
                    widgets = listOf(
                        TestCaseWidgetTabWidgetDto("widget-1", "Widget 1", 0, false),
                        TestCaseWidgetTabWidgetDto("widget-2", "Widget 2", 1, true)
                    )
                )
            )
        }

        val widgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(widgetTab).isNotNull
        assertThat(widgetTab!!.widgets).hasSize(2)

        runWithoutAuthorization {
            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey
                )
            )
        }

        val updatedWidgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(updatedWidgetTab).isNotNull
        assertThat(updatedWidgetTab!!.widgets).isEmpty()
    }

    @Test
    fun `should change order of widgets for widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        runWithoutAuthorization {
            caseTabService.createCaseTab(
                caseDefinitionName,
                CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-")
            )

            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey,
                    widgets = listOf(
                        TestCaseWidgetTabWidgetDto("widget-1", "Widget 1", 0, false),
                        TestCaseWidgetTabWidgetDto("widget-2", "Widget 2", 1, true)
                    )
                )
            )
        }

        val widgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(widgetTab).isNotNull

        assertThat(widgetTab!!.widgets).hasSize(2)
        assertThat(widgetTab.widgets[0].key).isEqualTo("widget-1")
        assertThat(widgetTab.widgets[1].key).isEqualTo("widget-2")
        assertThat(widgetTab.widgets[0].order).isEqualTo(0)
        assertThat(widgetTab.widgets[1].order).isEqualTo(1)

        runWithoutAuthorization {
            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey,
                    widgets = listOf(
                        TestCaseWidgetTabWidgetDto("widget-2", "Widget 2", 1, true),
                        TestCaseWidgetTabWidgetDto("widget-1", "Widget 1", 0, false)
                    )
                )
            )
        }

        val updatedWidgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(updatedWidgetTab).isNotNull

        assertThat(updatedWidgetTab!!.widgets).hasSize(2)
        assertThat(updatedWidgetTab.widgets[0].key).isEqualTo("widget-2")
        assertThat(updatedWidgetTab.widgets[1].key).isEqualTo("widget-1")
        assertThat(updatedWidgetTab.widgets[0].order).isEqualTo(0)
        assertThat(updatedWidgetTab.widgets[1].order).isEqualTo(1)
    }

}
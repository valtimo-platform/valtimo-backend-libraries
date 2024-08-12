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
import com.ritense.case_.widget.TestCaseWidgetProperties
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseWidgetTabServiceIntTest @Autowired constructor(
    private val caseTabService: CaseTabService,
    private val caseWidgetTabRepository: CaseWidgetTabRepository,
    private val caseWidgetTabService: CaseWidgetTabService,
    private val documentService: DocumentService
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
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should get widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        createCaseWidgetTab(caseDefinitionName, tabKey)

        val widgetTab = caseWidgetTabService.getWidgetTab(caseDefinitionName, tabKey)
        assertThat(widgetTab).isNotNull
        assertThat(widgetTab!!.widgets).hasSize(2)
        assertThat(widgetTab.widgets.map { it.key }).doesNotContain("deny")
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

        createCaseWidgetTab(caseDefinitionName, tabKey)

        val widgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(widgetTab).isNotNull

        assertThat(widgetTab!!.widgets).hasSize(3)
        assertThat(widgetTab.widgets[0].id.key).isEqualTo("widget-1")
        assertThat(widgetTab.widgets[1].id.key).isEqualTo("widget-2")
        assertThat(widgetTab.widgets[2].id.key).isEqualTo("deny")
    }

    @Test
    fun `should support equal widgets keys on different tabs`() {
        val caseDefinitionName = "some-case-type"

        val firstTab = "first-tab"
        createCaseWidgetTab(caseDefinitionName, firstTab)
        val secondTab = "second-tab"
        createCaseWidgetTab(caseDefinitionName, secondTab)
        val widgets = caseWidgetTabRepository.findAll()
            .filter { it.id.key in listOf(firstTab, secondTab) }
            .flatMap { it.widgets }
            .filter { it.id.key == "widget-1" }

        assertThat(widgets).hasSize(2)
        assertThat(widgets.singleOrNull { it.id.caseWidgetTab!!.id.key == firstTab }).isNotNull
        assertThat(widgets.singleOrNull { it.id.caseWidgetTab!!.id.key == secondTab }).isNotNull
    }

    @Test
    fun `should remove widgets from widget tab`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        createCaseWidgetTab(caseDefinitionName, tabKey)

        val widgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(widgetTab).isNotNull
        assertThat(widgetTab!!.widgets).isNotEmpty

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

        createCaseWidgetTab(caseDefinitionName, tabKey)

        val widgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(widgetTab).isNotNull

        assertThat(widgetTab!!.widgets).hasSize(3)
        assertThat(widgetTab.widgets[0].id.key).isEqualTo("widget-1")
        assertThat(widgetTab.widgets[1].id.key).isEqualTo("widget-2")
        assertThat(widgetTab.widgets[0].order).isEqualTo(0)
        assertThat(widgetTab.widgets[1].order).isEqualTo(1)

        runWithoutAuthorization {
            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey,
                    widgets = listOf(
                        TestCaseWidgetTabWidgetDto("widget-2", "Widget 2", 2, true, TestCaseWidgetProperties("test123")),
                        TestCaseWidgetTabWidgetDto("widget-1", "Widget 1", 1, false, TestCaseWidgetProperties("test123"))
                    )
                )
            )
        }

        val updatedWidgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey))

        assertThat(updatedWidgetTab).isNotNull

        assertThat(updatedWidgetTab!!.widgets).hasSize(2)
        assertThat(updatedWidgetTab.widgets[0].id.key).isEqualTo("widget-2")
        assertThat(updatedWidgetTab.widgets[1].id.key).isEqualTo("widget-1")
        assertThat(updatedWidgetTab.widgets[0].order).isEqualTo(0)
        assertThat(updatedWidgetTab.widgets[1].order).isEqualTo(1)
    }


    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should get data for widget`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        createCaseWidgetTab(caseDefinitionName, tabKey)
        val documentId = createDocument(caseDefinitionName).id().id

        val widgetData = caseWidgetTabService.getCaseWidgetData(documentId, tabKey, "widget-1", Pageable.unpaged())
        assertThat(widgetData).isInstanceOf(Map::class.java)
        assertThat((widgetData as Map<String, Any>)).containsEntry("test", "test123")
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should deny data for widget`() {
        val caseDefinitionName = "some-case-type"
        val tabKey = "my-tab"

        createCaseWidgetTab(caseDefinitionName, tabKey)
        val documentId = createDocument(caseDefinitionName).id().id

        assertThrows<AccessDeniedException> {
            caseWidgetTabService.getCaseWidgetData(documentId, tabKey, "deny", Pageable.unpaged())
        }
    }

    private fun createCaseWidgetTab(caseDefinitionName: String, tabKey: String): CaseWidgetTabDto? {
        return runWithoutAuthorization {
            caseTabService.createCaseTab(
                caseDefinitionName,
                CaseTabDto(key = tabKey, type = CaseTabType.WIDGETS, contentKey = "-")
            )

            caseWidgetTabService.updateWidgetTab(
                CaseWidgetTabDto(
                    caseDefinitionName,
                    tabKey,
                    widgets = listOf(
                        TestCaseWidgetTabWidgetDto("widget-1", "Widget 1", 1, false),
                        TestCaseWidgetTabWidgetDto("widget-2", "Widget 2", 2, true),
                        TestCaseWidgetTabWidgetDto("deny", "Deny", 3, false)
                    )
                )
            )
        }
    }

    private fun createDocument(documentDefinitionName: String, content: String = "{}"): Document {
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    documentDefinitionName,
                    JsonDocumentContent(content).asJson()
                )
            ).resultingDocument().orElseThrow()
        }
    }
}
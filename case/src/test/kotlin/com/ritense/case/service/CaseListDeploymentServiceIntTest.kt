package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DateFormatDisplayTypeParameter
import com.ritense.case.service.CaseListDeploymentService.Companion.CASE_LIST_DEFINITIONS_PATH
import com.ritense.case.service.CaseListDeploymentService.Companion.CASE_LIST_SCHEMA_PATH
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaseListDeploymentServiceIntTest: BaseIntegrationTest() {
    @Autowired
    lateinit var caseDefinitionService: CaseDefinitionService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should load columns on application startup`() {
        val listColumns = caseDefinitionService.getListColumns("house")
        val firstColumn = listColumns[0]

        assertEquals("some-title", firstColumn.title)
        assertEquals("test", firstColumn.key)
        assertEquals("case:createdOn", firstColumn.path)
        assertEquals("date", firstColumn.displayType.type)
        assertTrue(firstColumn.displayType.displayTypeParameters is DateFormatDisplayTypeParameter)
        assertEquals("yyyy-MM-dd",
            (firstColumn.displayType.displayTypeParameters as DateFormatDisplayTypeParameter).dateFormat)
        assertFalse(firstColumn.sortable)
        assertEquals(ColumnDefaultSort.DESC, firstColumn.defaultSort)
        assertEquals(0, firstColumn.order)
    }


    @Test
    fun `should delete old columns in database when loading new configuration`() {
        //load initial configuration
        val spyResolver = spy(resourcePatternResolver)
        val resource = mock<Resource>()
        val fileContent = """
            [
                {
                    "key": "old",
                    "title": "old-title",
                    "path": "case:createdOn",
                    "displayType": {
                        "type": "date",
                        "displayTypeParameters": {
                            "dateFormat": "yyyy-dd-MM"
                        }
                    },
                    "sortable": true,
                    "defaultSort": "ASC",
                    "order": 0
                }
            ]
        """.trimIndent()
        whenever(resource.filename).thenReturn("some-document.json")
        whenever(resource.inputStream).thenReturn(fileContent.byteInputStream())

        doReturn(arrayOf(resource)).whenever(spyResolver).getResources(CASE_LIST_DEFINITIONS_PATH)
        doCallRealMethod().whenever(spyResolver).getResource(CASE_LIST_SCHEMA_PATH)

        val service = CaseListDeploymentService(
            spyResolver,
            objectMapper,
            caseDefinitionService
        )

        service.deployColumns()

        //override configuration by loading new file
        val newResource = mock<Resource>()
        val newFileContent = """
            [
                {
                    "key": "test",
                    "title": "some-title",
                    "path": "case:createdOn",
                    "displayType": {
                        "type": "date",
                        "displayTypeParameters": {
                            "dateFormat": "yyyy-MM-dd"
                        }
                    },
                    "sortable": false,
                    "defaultSort": "DESC",
                    "order": 0
                }
            ]
        """.trimIndent()
        whenever(newResource.filename).thenReturn("some-document.json")
        whenever(newResource.inputStream).thenReturn(newFileContent.byteInputStream())

        doReturn(arrayOf(newResource)).whenever(spyResolver).getResources(CASE_LIST_DEFINITIONS_PATH)

        service.deployColumns()

        val listColumns = caseDefinitionService.getListColumns("some-document")
        val firstColumn = listColumns[0]

        assertEquals(1, listColumns.size)
        assertEquals("some-title", firstColumn.title)
        assertEquals("test", firstColumn.key)
        assertEquals("case:createdOn", firstColumn.path)
        assertEquals("date", firstColumn.displayType.type)
        assertTrue(firstColumn.displayType.displayTypeParameters is DateFormatDisplayTypeParameter)
        assertEquals("yyyy-MM-dd",
            (firstColumn.displayType.displayTypeParameters as DateFormatDisplayTypeParameter).dateFormat)
        assertFalse(firstColumn.sortable)
        assertEquals(ColumnDefaultSort.DESC, firstColumn.defaultSort)
        assertEquals(0, firstColumn.order)
    }
}
/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.valtimo.formflow.service

import com.ritense.formflow.service.FormFlowService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.formflow.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Transactional
internal class FormFlowSupportedProcessLinkIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowSupportedProcessLinks: FormFlowSupportedProcessLinksHandler

    @Test
    fun `should return a form flow process link type for StartEventStart with enabled false`() {
        val formFlowServiceMock = mock<FormFlowService>()
        whenever(formFlowServiceMock.getFormFlowDefinitions()).thenReturn(emptyList())
        val formFlowSupportedProcessLinks = FormFlowSupportedProcessLinksHandler(formFlowServiceMock)
        val result = formFlowSupportedProcessLinks.getProcessLinkType(ActivityTypeWithEventName.START_EVENT_START.value)
        assertEquals("form_flow", result?.processLinkType)
        assertEquals(false, result?.enabled)
    }

    @Test
    fun `should return a form flow process link type for StartEventStart with enabled true`() {
        val result = formFlowSupportedProcessLinks.getProcessLinkType(ActivityTypeWithEventName.START_EVENT_START.value)
        assertEquals("form_flow", result?.processLinkType)
        assertEquals(true, result?.enabled)
    }

    @Test
    fun `should not return a form flow process link type for ServiceTaskStart`() {
        val result = formFlowSupportedProcessLinks.getProcessLinkType(ActivityTypeWithEventName.SERVICE_TASK_START.value)
        assertNull(result)
    }

}
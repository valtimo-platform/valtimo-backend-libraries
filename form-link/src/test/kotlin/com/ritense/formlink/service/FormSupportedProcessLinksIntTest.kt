/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.formlink.service

import com.ritense.form.domain.request.CreateFormDefinitionRequest
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formlink.BaseIntegrationTest
import com.ritense.processlink.domain.ActivityTypeWithEventName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Transactional
internal class FormSupportedProcessLinksIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var formSupportedProcessLinks: FormSupportedProcessLinks

    @Autowired
    lateinit var formDefinitionService: FormIoFormDefinitionService

    @Test
    fun `should return a form process link type for StartEventStart with enabled false`() {
        val result = formSupportedProcessLinks.getProcessLinkType(ActivityTypeWithEventName.START_EVENT_START.value)
        assertEquals("form", result?.processLinkType)
        assertEquals(false, result?.enabled)
    }

    @Test
    fun `should return a form process link type for StartEventStart with enabled true`() {
        formDefinitionService.createFormDefinition(
            CreateFormDefinitionRequest(
                "FormName",
                "{}",
                false
            )
        )
        val result = formSupportedProcessLinks.getProcessLinkType(ActivityTypeWithEventName.START_EVENT_START.value)
        assertEquals("form", result?.processLinkType)
        assertEquals(true, result?.enabled)
    }

    @Test
    fun `should not return a form process link type for ServiceTaskStart`() {
        val result = formSupportedProcessLinks.getProcessLinkType(ActivityTypeWithEventName.SERVICE_TASK_START.value)
        assertNull(result)
    }


}

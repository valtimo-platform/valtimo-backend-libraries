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

package com.ritense.form.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.form.BaseIntegrationTest
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.request.CreateFormDefinitionRequest
import com.ritense.form.processlink.FormProcessLinkActivityHandler
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Transactional
internal class FormProcessLinkActivityHandlerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var formDefinitionService: FormIoFormDefinitionService

    @Autowired
    lateinit var formProcessLinkActivityHandler: FormProcessLinkActivityHandler

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should retrieve form definition`() {
        val formDefinition = formDefinitionService.createFormDefinition(
            CreateFormDefinitionRequest(
                "aName",
                getForm(),
                false
            )
        )
        val processDefinitionId: String = UUID.randomUUID().toString()
        val processLinkId = UUID.randomUUID()
        val processLink: ProcessLink = FormProcessLink(
            id = processLinkId,
            processDefinitionId = processDefinitionId,
            activityId = "some_activity_id",
            activityType = ActivityTypeWithEventName.START_EVENT_START,
            formDefinitionId = UUID.fromString(formDefinition.id?.toString())
        )
        val result = formProcessLinkActivityHandler.getStartEventObject(
            "",
            processLink
        )
        assertEquals("form",result.type)
        assertEquals(formDefinition.id?.toString(),result.properties.formDefinitionId.toString())
        assertEquals(getForm(),objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.properties.prefilledForm))
        val x = true
    }

    private fun getForm(): String{
        return """
            {
              "display" : "form",
              "components" : [ {
                "label" : "back",
                "tableView" : false,
                "key" : "pv.something",
                "type" : "hidden",
                "input" : true
              } ]
            }
        """.trimIndent()
    }
}

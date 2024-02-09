/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.processlink.service

import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.TestPlugin
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class PluginServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Test
    fun `should call plugin action with parameter from process variable when exists`() {
        runtimeService.startProcessInstanceByKey(
            "simple-process",
            "myBusinessKey",
            mapOf("attachmentIds" to listOf("ID-1", "ID-2"))
        )

        assertEquals(listOf("ID-1", "ID-2"), TestPlugin.attachmentIds)
    }

    @Test
    fun `should call plugin action with empty parameter from NULL process variable`() {
        runtimeService.startProcessInstanceByKey(
            "simple-process",
            "myBusinessKey",
            mapOf("attachmentIds" to null)
        )

        assertNull(TestPlugin.attachmentIds)
    }

    @Test
    fun `should call plugin action with empty parameter from process variable when not exists`() {
        runtimeService.startProcessInstanceByKey(
            "simple-process",
            "myBusinessKey",
            mapOf()
        )

        assertNull(TestPlugin.attachmentIds)
    }


}
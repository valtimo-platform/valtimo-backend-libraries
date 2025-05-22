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

package com.ritense.smartdocuments.plugin

import com.ritense.processdocument.service.DocumentDelegateService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.domain.DocumentsStructure
import com.ritense.smartdocuments.domain.SmartDocumentsTemplateData
import com.ritense.smartdocuments.domain.Template
import com.ritense.smartdocuments.domain.TemplateGroup
import com.ritense.smartdocuments.domain.TemplatesStructure
import com.ritense.valueresolver.ValueResolverService
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

const val TEMPLATE_NAME_LIST = "templateNameList"
const val TEMPLATE_GROUP_NAME = "Group 1"
const val TEMPLATE_NAME = "Template 1"

@ExtendWith(MockitoExtension::class)
internal class SmartDocumentsPluginTest {

    @Mock
    lateinit var documentDelegateService: DocumentDelegateService

    @Mock
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Mock
    lateinit var smartDocumentsClient: SmartDocumentsClient

    @Mock
    lateinit var valueResolverService: ValueResolverService

    @Mock
    lateinit var temporaryResourceStorageService: TemporaryResourceStorageService

    @InjectMocks
    lateinit var smartDocumentsPlugin: SmartDocumentsPlugin

    private lateinit var delegateExecution: DelegateExecution

    @BeforeEach
    fun setup() {
        // given
        delegateExecution = delegateExecution()
    }

    @Test
    fun `should get template names`() {
        // given
        whenever(smartDocumentsClient.getSmartDocumentsTemplateData(any())).thenReturn(smartDocumentsTemplateData())
        smartDocumentsPlugin.url = "test.com"
        smartDocumentsPlugin.username = "username"
        smartDocumentsPlugin.password = "password"

        // when
        smartDocumentsPlugin.getTemplateNames(
            execution = delegateExecution,
            templateGroupName = TEMPLATE_GROUP_NAME,
            resultingTemplateNameListProcessVariableName = TEMPLATE_NAME_LIST
        )

        // then
        val result = delegateExecution.getVariable(TEMPLATE_NAME_LIST) as List<String>

        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(3)
        assertThat(result.first()).isEqualTo(TEMPLATE_NAME)

    }@Test
    fun `list should be empty`() {
        // given
        whenever(smartDocumentsClient.getSmartDocumentsTemplateData(any())).thenReturn(smartDocumentsTemplateData())
        smartDocumentsPlugin.url = "test.com"
        smartDocumentsPlugin.username = "username"
        smartDocumentsPlugin.password = "password"

        // when
        smartDocumentsPlugin.getTemplateNames(
            execution = delegateExecution,
            templateGroupName = "Nope",
            resultingTemplateNameListProcessVariableName = TEMPLATE_NAME_LIST
        )

        // then
        val result = delegateExecution.getVariable(TEMPLATE_NAME_LIST) as List<String>

        assertThat(result).isNotNull
        assertThat(result).isEmpty()
    }

    private fun smartDocumentsTemplateData() = SmartDocumentsTemplateData(
        DocumentsStructure(
            TemplatesStructure(
                listOf(
                    TemplateGroup(
                        TEMPLATE_GROUP_NAME,
                        null,
                        listOf(
                            Template(
                                "BA72ACC982C042A5B285DF91F684C214",
                                TEMPLATE_NAME
                            ),
                            Template(
                                "6B39F51603474130B8DF7CE7ED58309F",
                                "Plan intakegesprek1"
                            ),
                            Template(
                                "9014A7F2AD12453DBE2AE055773642E0",
                                "Plan intakegesprek2"
                            )
                        )
                    ),
                    TemplateGroup(
                        "test",
                        null,
                        listOf(
                            Template(
                                "A99A1DD46F204EDA9988EE7F54C99B6E",
                                "Plan intakegesprek3"
                            ),
                            Template(
                                "E9BBADF6C0964CB69A0165E26509DAF0",
                                "Plan intakegesprek4"
                            )
                        )
                    )
                )
            )
        )
    )

    private fun delegateExecution(): DelegateExecution {
        return DelegateExecutionFake()
            .withBusinessKey("business_key")
            .withProcessInstanceId("process_instance_id")
    }
}
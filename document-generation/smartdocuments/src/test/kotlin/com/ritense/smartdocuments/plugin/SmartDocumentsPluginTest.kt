package com.ritense.smartdocuments.plugin

import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.domain.*
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockitoExtension::class)
internal class SmartDocumentsPluginTest {

    @Mock
    lateinit var processDocumentService: ProcessDocumentService

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
        whenever(smartDocumentsClient.getDocumentStructure()).thenReturn(documentsStructure())

        // when
        val result = smartDocumentsPlugin.getTemplateNames("Group 1")

        // then
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(2)
        assertThat(result.first()).isEqualTo("Template 1")

    }@Test
    fun `list should be empty`() {
        // given
        whenever(smartDocumentsClient.getDocumentStructure()).thenReturn(documentsStructure())

        // when
        val result = smartDocumentsPlugin.getTemplateNames("No group")

        // then
        assertThat(result).isNotNull
        assertThat(result).isEmpty()
    }

    private fun documentsStructure(): DocumentsStructure = DocumentsStructure(
        templatesStructure = TemplatesStructure(
            isAccessible = "true",
            templateGroups = listOf(
                TemplateGroup(
                    isAccessible = "true",
                    id = "group1",
                    name = "Group 1",
                    templateGroups = emptyList(),
                    templates = listOf(
                        Template(id = "template1", name = "Template 1"),
                        Template(id = "template2", name = "Template 2")
                    )
                ),
                TemplateGroup(
                    isAccessible = "true",
                    id = "group2",
                    name = "Group 2",
                    templateGroups = emptyList(),
                    templates = listOf(
                        Template(id = "template3", name = "Template 3"),
                        Template(id = "template4", name = "Template 4")
                    )
                )
            )
        ),
        usersStructure = UsersStructure(
            isAccessible = "true",
            groupsAccess = GroupsAccess(
                templateGroups = emptyList(),
                headerGroups = emptyList()
            ),
            userGroups = UserGroups(
                userGroup = UserGroup(
                    isAccessible = "true",
                    id = "userGroup1",
                    name = "User Group 1",
                    groupsAccess = GroupsAccess(
                        templateGroups = emptyList(),
                        headerGroups = emptyList()
                    ),
                    userGroups = emptyList(),
                    users = Users(
                        user = User(
                            id = "user1",
                            name = "John Doe"
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
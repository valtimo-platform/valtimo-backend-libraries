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

package com.ritense

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.case.deployment.CaseTaskListDeploymentService
import com.ritense.case.service.CaseTabImporter
import com.ritense.document.domain.Document
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.domain.impl.searchfield.SearchField
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot
import com.ritense.document.repository.SearchFieldRepository
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentSearchService
import com.ritense.document.service.JsonSchemaDocumentActionProvider
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider
import com.ritense.document.service.JsonSchemaDocumentSnapshotActionProvider
import com.ritense.document.service.SearchFieldActionProvider
import com.ritense.document.service.SearchFieldService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.outbox.OutboxService
import com.ritense.resource.service.ResourceService
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.service.ProcessDefinitionCaseDefinitionLinker
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.List
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RecordApplicationEvents
@SpringBootTest
@ExtendWith(SpringExtension::class, LiquibaseRunnerExtension::class)
@Tag("integration")
class BaseIntegrationTest: BaseTest() {

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    lateinit var userManagementService: UserManagementService

    @MockitoBean
    lateinit var applicationEventMulticaster: SimpleApplicationEventMulticaster

    @MockitoBean
    lateinit var resourceService: ResourceService

    @MockitoBean
    lateinit var mailSender: MailSender

    @MockitoBean
    lateinit var processDefinitionCaseDefinitionLinker: ProcessDefinitionCaseDefinitionLinker

    @MockitoSpyBean
    lateinit var resourcePatternResolver: ResourcePatternResolver

    @MockitoSpyBean
    lateinit var caseTabImporter: CaseTabImporter

    @MockitoSpyBean
    lateinit var caseTaskListDeploymentService: CaseTaskListDeploymentService

    @MockitoSpyBean
    lateinit var outboxService: OutboxService

    @MockitoSpyBean
    lateinit var documentRepository: JsonSchemaDocumentRepository

    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var documentService: JsonSchemaDocumentService

    @Autowired
    lateinit var documentSearchService: DocumentSearchService

    @Autowired
    lateinit var searchFieldService: SearchFieldService

    @Autowired
    lateinit var searchFieldRepository: SearchFieldRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var events: ApplicationEvents

    @BeforeAll
    fun beforeAll() {
    }

    @BeforeEach
    fun beforeEachBase() {
        setUpPermissions()
    }

    protected fun mockUser(firstName: String?, lastName: String?): ManageableUser {
        return ValtimoUserBuilder()
            .id(UUID.randomUUID().toString())
            .firstName(firstName)
            .lastName(lastName)
            .roles(List.of(FULL_ACCESS_ROLE))
            .build()
    }

    protected fun createDocument(documentDefinition: DocumentDefinition, content: String?): Document {
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    documentDefinition.id().name(),
                    documentDefinition.id().name(),
                    "1.0.0",
                    JsonDocumentContent(content).asJson()
                )
            ).resultingDocument().orElseThrow()
        }
    }

    private fun setUpPermissions() {
        var role = roleRepository.findByKey(FULL_ACCESS_ROLE)

        if (role == null) {
            role = roleRepository.save(Role(UUID.randomUUID(), FULL_ACCESS_ROLE))
        }

        val permissions = List.of(
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.VIEW_LIST,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.VIEW,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.MODIFY,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.CREATE,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.CLAIM,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.ASSIGN,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.ASSIGNABLE,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.DELETE,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                SearchField::class.java,
                SearchFieldActionProvider.VIEW_LIST,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition::class.java,
                JsonSchemaDocumentDefinitionActionProvider.VIEW,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition::class.java,
                JsonSchemaDocumentDefinitionActionProvider.VIEW_LIST,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition::class.java,
                JsonSchemaDocumentDefinitionActionProvider.CREATE,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition::class.java,
                JsonSchemaDocumentDefinitionActionProvider.MODIFY,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition::class.java,
                JsonSchemaDocumentDefinitionActionProvider.DELETE,
                ConditionContainer(emptyList()),
                role!!
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentSnapshot::class.java,
                JsonSchemaDocumentSnapshotActionProvider.VIEW_LIST,
                ConditionContainer(emptyList()),
                role!!
            )
        )

        permissionRepository.saveAll(permissions)
    }

    companion object {
        const val FULL_ACCESS_ROLE: String = "full access role"
    }
}


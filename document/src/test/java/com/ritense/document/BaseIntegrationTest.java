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

package com.ritense.document;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.permission.PermissionRepository;
import com.ritense.authorization.role.Role;
import com.ritense.authorization.role.RoleRepository;
import com.ritense.authorization.permission.ConditionContainer;
import com.ritense.authorization.permission.Permission;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.repository.DocumentSnapshotRepository;
import com.ritense.document.repository.SearchFieldRepository;
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.DocumentSnapshotService;
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider;
import com.ritense.document.service.JsonSchemaDocumentSnapshotActionProvider;
import com.ritense.document.service.SearchFieldActionProvider;
import com.ritense.document.service.SearchFieldService;
import com.ritense.resource.service.ResourceService;
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.ASSIGN;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.ASSIGNABLE;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.CLAIM;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.CREATE;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW_LIST;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.MODIFY;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW;

@SpringBootTest
@Tag("integration")
@ExtendWith({SpringExtension.class, LiquibaseRunnerExtension.class})
public abstract class BaseIntegrationTest extends BaseTest {

    @Inject
    protected DocumentDefinitionService documentDefinitionService;

    @Inject
    protected DocumentService documentService;

    @Inject
    protected JsonSchemaDocumentRepository documentRepository;

    @Inject
    protected DocumentSearchService documentSearchService;

    @Inject
    protected DocumentSnapshotService documentSnapshotService;

    @Inject
    protected DocumentSnapshotRepository<JsonSchemaDocumentSnapshot> documentSnapshotRepository;

    @Inject
    protected SearchFieldService searchFieldService;

    @Inject
    protected SearchFieldRepository searchFieldRepository;

    @Inject
    protected RoleRepository roleRepository;

    @Inject
    protected PermissionRepository permissionRepository;

    @MockBean
    public ResourceService resourceService;

    @MockBean
    protected UserManagementService userManagementService;

    @MockBean
    public SimpleApplicationEventMulticaster applicationEventMulticaster;
    protected static final String FULL_ACCESS_ROLE = "full access role";

    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    public void beforeEachBase() {
        setUpPermissions();
    }

    @AfterEach
    public void afterEach() {
        permissionRepository.deleteAll();
        roleRepository.deleteAll();
    }

    protected ManageableUser mockUser(String firstName, String lastName) {
        return new ValtimoUserBuilder()
            .id(UUID.randomUUID().toString())
            .firstName(firstName)
            .lastName(lastName)
            .build();
    }

    protected Document createDocument(DocumentDefinition documentDefinition, String content) {
        return AuthorizationContext
            .runWithoutAuthorization(
                () -> documentService.createDocument(
                    new NewDocumentRequest(
                        documentDefinition.id().name(),
                        new JsonDocumentContent(content).asJson()
                    )
                ).resultingDocument().orElseThrow()
            );
    }

    private void setUpPermissions() {
        Role role = roleRepository.findByKey(FULL_ACCESS_ROLE);

        if (role == null) {
            role = roleRepository.save(new Role(UUID.randomUUID(), FULL_ACCESS_ROLE));
        }

        List<Permission> permissions = List.of(
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                VIEW_LIST,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                VIEW,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                MODIFY,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                CREATE,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                CLAIM,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                ASSIGN,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                ASSIGNABLE,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                SearchField.class,
                SearchFieldActionProvider.VIEW_LIST,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition.class,
                JsonSchemaDocumentDefinitionActionProvider.VIEW,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition.class,
                JsonSchemaDocumentDefinitionActionProvider.VIEW_LIST,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition.class,
                JsonSchemaDocumentDefinitionActionProvider.CREATE,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition.class,
                JsonSchemaDocumentDefinitionActionProvider.MODIFY,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentDefinition.class,
                JsonSchemaDocumentDefinitionActionProvider.DELETE,
                new ConditionContainer(Collections.emptyList()),
                role
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocumentSnapshot.class,
                JsonSchemaDocumentSnapshotActionProvider.VIEW_LIST,
                new ConditionContainer(Collections.emptyList()),
                role
            )
        );

        permissionRepository.saveAll(permissions);
    }
}

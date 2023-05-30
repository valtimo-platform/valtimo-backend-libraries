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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.PermissionRepository;
import com.ritense.authorization.Role;
import com.ritense.authorization.RoleRepository;
import com.ritense.authorization.permission.ConditionContainer;
import com.ritense.authorization.permission.Permission;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.repository.DocumentSnapshotRepository;
import com.ritense.document.repository.SearchFieldRepository;
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.DocumentSnapshotService;
import com.ritense.document.service.SearchFieldService;
import com.ritense.resource.service.ResourceService;
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    protected static final String ROLE1 = "test-role-1";
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
        roleRepository.save(new Role(ROLE1));
        roleRepository.save(new Role(FULL_ACCESS_ROLE));

        List<Permission> permissions = List.of(
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                Action.LIST_VIEW,
                new ConditionContainer(Collections.emptyList()),
                FULL_ACCESS_ROLE
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                Action.VIEW,
                new ConditionContainer(Collections.emptyList()),
                FULL_ACCESS_ROLE
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                Action.MODIFY,
                new ConditionContainer(Collections.emptyList()),
                FULL_ACCESS_ROLE
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                Action.CREATE,
                new ConditionContainer(Collections.emptyList()),
                FULL_ACCESS_ROLE
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                Action.CLAIM,
                new ConditionContainer(Collections.emptyList()),
                FULL_ACCESS_ROLE
            ),
            new Permission(
                UUID.randomUUID(),
                JsonSchemaDocument.class,
                Action.ASSIGN,
                new ConditionContainer(Collections.emptyList()),
                FULL_ACCESS_ROLE
            )
        );

        permissionRepository.saveAllAndFlush(permissions);
    }
}

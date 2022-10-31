/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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
import java.util.UUID;

@SpringBootTest
@Tag("integration")
@ExtendWith(SpringExtension.class)
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

    @MockBean
    public ResourceService resourceService;

    @MockBean
    protected UserManagementService userManagementService;

    @MockBean
    public SimpleApplicationEventMulticaster applicationEventMulticaster;

    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    public void beforeEach() {
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
}

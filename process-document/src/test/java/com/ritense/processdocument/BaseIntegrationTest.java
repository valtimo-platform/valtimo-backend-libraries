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

package com.ritense.processdocument;

import com.ritense.audit.service.impl.AuditServiceImpl;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentAssociationService;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.service.CamundaTaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;

@SpringBootTest
@Tag("integration")
@ExtendWith(SpringExtension.class)
public abstract class BaseIntegrationTest extends BaseTest {

    @MockBean
    protected UserManagementService userManagementService;

    @Inject
    protected CamundaProcessJsonSchemaDocumentAssociationService camundaProcessJsonSchemaDocumentAssociationService;

    @Inject
    protected CamundaProcessJsonSchemaDocumentService camundaProcessJsonSchemaDocumentService;

    @Inject
    protected CamundaTaskService camundaTaskService;

    @MockBean
    protected AuditServiceImpl auditService;

    @MockBean
    protected ResourceService resourceService;

    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    public void beforeEach() {
    }

    @AfterEach
    public void afterEach() {
    }

}

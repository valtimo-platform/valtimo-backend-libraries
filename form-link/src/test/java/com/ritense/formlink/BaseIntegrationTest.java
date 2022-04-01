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

package com.ritense.formlink;

import com.ritense.document.service.DocumentSnapshotService;
import com.ritense.form.autodeployment.FormApplicationReadyEventListener;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.formlink.repository.ProcessFormAssociationRepository;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.mail.MailSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import javax.inject.Inject;
import java.io.IOException;

@SpringBootTest
@Tag("integration")
@ExtendWith(SpringExtension.class)
public abstract class BaseIntegrationTest extends BaseTest {

    @MockBean
    protected UserManagementService userManagementService;

    @MockBean
    protected DocumentSnapshotService documentSnapshotService;

    @SpyBean
    public MailSender mailSender;

    @Inject
    public ProcessFormAssociationRepository processFormAssociationRepository;

    @Inject
    public FormDefinitionRepository formDefinitionRepository;

    @MockBean
    public FormApplicationReadyEventListener formApplicationReadyEventListener;

    @BeforeAll
    public static void beforeAll() {
    }

    @BeforeEach
    public void beforeEach() throws IOException {
    }

    @AfterEach
    public void afterEach() {
    }

}

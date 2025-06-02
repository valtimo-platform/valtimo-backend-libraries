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

package com.ritense.processdocument.service.impl;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.utils.TestUtil.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

import com.ritense.processdocument.BaseIntegrationTest;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest;
import com.ritense.processdocument.repository.CaseDefinitionProcessLinkRepository;
import com.ritense.processdocument.service.CaseDefinitionProcessLinkService;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CaseDefinitionProcessLinkServiceIntTest extends BaseIntegrationTest {

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final CaseDefinitionId CASE_DEFINITION_ID = new CaseDefinitionId(DOCUMENT_DEFINITION_NAME, "1.0.0");
    private static final String DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
    private static final String PROCESS_DEFINITION_KEY = "loan-process-demo";

    @Autowired
    private CaseDefinitionProcessLinkRepository caseDefinitionProcessLinkRepository;

    @Autowired
    private CaseDefinitionProcessLinkService caseDefinitionProcessLinkService;

    @BeforeEach
    void beforeEach() {
        runWithoutAuthorization(() ->
            caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
                CASE_DEFINITION_ID,
                new DocumentDefinitionProcessRequest(
                    PROCESS_DEFINITION_KEY,
                    DOCUMENT_UPLOAD
                )
            )
        );
    }

    @Test
    void shouldGetDocumentDefinitionProcessLink() {
        var link = caseDefinitionProcessLinkService.getDocumentDefinitionProcessLink(
            CASE_DEFINITION_ID,
            DOCUMENT_UPLOAD
        );

        assertThat(link).isNotNull();
        assertThat(link.getId().getCaseDefinitionId()).isEqualTo(CASE_DEFINITION_ID);
        assertThat(link.getId().getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(link.getType()).isEqualTo(DOCUMENT_UPLOAD);
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL, authorities = ADMIN)
    void shouldOverrideProcessDefinitionKeyInLinkWhenSaving() {
        caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            CASE_DEFINITION_ID,
            new DocumentDefinitionProcessRequest(
                "embedded-subprocess-example",
                DOCUMENT_UPLOAD
            )
        );

        var caseDefinitionProcess = caseDefinitionProcessLinkService.getDocumentDefinitionProcess(
            CASE_DEFINITION_ID,
            DOCUMENT_UPLOAD
        );

        assertThat(caseDefinitionProcess).isNotNull();
        assertThat(caseDefinitionProcess.getProcessDefinitionKey()).isEqualTo("embedded-subprocess-example");
    }
}

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

import static org.assertj.core.api.Assertions.assertThat;

import com.ritense.processdocument.BaseIntegrationTest;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest;
import com.ritense.processdocument.repository.CaseDefinitionProcessLinkRepository;
import com.ritense.processdocument.service.CaseDefinitionProcessLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CaseDefinitionProcessLinkServiceIntTest extends BaseIntegrationTest {

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final String DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
    private static final String PROCESS_DEFINITION_KEY = "loan-process-demo";

    @Autowired
    private CaseDefinitionProcessLinkRepository caseDefinitionProcessLinkRepository;

    @Autowired
    private CaseDefinitionProcessLinkService caseDefinitionProcessLinkService;

    @BeforeEach
    public void beforeEach() {
        caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            DOCUMENT_DEFINITION_NAME,
            new DocumentDefinitionProcessRequest(
                PROCESS_DEFINITION_KEY,
                DOCUMENT_UPLOAD
            )
        );
    }

    @Test
    void shouldGetDocumentDefinitionProcessLink() {
        var link = caseDefinitionProcessLinkService.getDocumentDefinitionProcessLink(
            DOCUMENT_DEFINITION_NAME,
            DOCUMENT_UPLOAD
        );

        assertThat(link.isPresent()).isTrue();
        assertThat(link.get().getId().getDocumentDefinitionName()).isEqualTo(DOCUMENT_DEFINITION_NAME);
        assertThat(link.get().getId().getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(link.get().getType()).isEqualTo(DOCUMENT_UPLOAD);
    }

    @Test
    void shouldGetDocumentDefinitionProcessLinkList() {
        caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            DOCUMENT_DEFINITION_NAME,
            new DocumentDefinitionProcessRequest(
                "embedded-subprocess-example",
                "my-other-link-type"
            )
        );

        var links = caseDefinitionProcessLinkService.getDocumentDefinitionProcessList(DOCUMENT_DEFINITION_NAME);

        assertThat(links.size()).isEqualTo(2);
        assertThat(links.get(0).processDefinitionKey).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(links.get(1).processDefinitionKey).isEqualTo("embedded-subprocess-example");
    }

    @Test
    void shouldOverrideProcessDefinitionKeyInLinkWhenSaving() {
        caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            DOCUMENT_DEFINITION_NAME,
            new DocumentDefinitionProcessRequest(
                "embedded-subprocess-example",
                DOCUMENT_UPLOAD
            )
        );

        var links = caseDefinitionProcessLinkService.getDocumentDefinitionProcessList(DOCUMENT_DEFINITION_NAME);

        assertThat(links.size()).isEqualTo(1);
        assertThat(links.get(0).processDefinitionKey).isEqualTo("embedded-subprocess-example");
    }

    @Test
    void shouldOverrideTypeInLinkWhenSaving() {
        caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            DOCUMENT_DEFINITION_NAME,
            new DocumentDefinitionProcessRequest(
                PROCESS_DEFINITION_KEY,
                "my-type"
            )
        );

        var links = caseDefinitionProcessLinkRepository.findAllByIdDocumentDefinitionName(DOCUMENT_DEFINITION_NAME);

        assertThat(links.size()).isEqualTo(1);
        assertThat(links.get(0).getType()).isEqualTo("my-type");
    }

}

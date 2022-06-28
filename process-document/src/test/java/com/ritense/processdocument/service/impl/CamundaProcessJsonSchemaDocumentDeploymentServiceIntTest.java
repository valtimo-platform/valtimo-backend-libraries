/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.processdocument.BaseIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
class CamundaProcessJsonSchemaDocumentDeploymentServiceIntTest extends BaseIntegrationTest {

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final String PROCESS_DEFINITION_KEY = "loan-process-demo";

    @Test
    void shouldDeployProcessDocumentLinkFromResourceFolder() {
        final var processDocumentDefinitions = camundaProcessJsonSchemaDocumentAssociationService
                .findProcessDocumentDefinitions(DOCUMENT_DEFINITION_NAME);

        assertThat(processDocumentDefinitions).hasSize(1);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().processDefinitionKey()).hasToString(PROCESS_DEFINITION_KEY);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().documentDefinitionId().name()).isEqualTo(DOCUMENT_DEFINITION_NAME);
        assertThat(processDocumentDefinitions.get(0).canInitializeDocument()).isTrue();
        assertThat(processDocumentDefinitions.get(0).startableByUser()).isTrue();
    }

    @Test
    void shouldDeployProcessContextFromResourceFolder() {
        var contextProcesses = contextService.findAll(Pageable.unpaged()).stream()
                .flatMap(c -> c.getProcesses().stream())
                .filter(cp -> cp.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY))
                .collect(Collectors.toList());

        assertThat(contextProcesses.size()).isEqualTo(1);
        assertThat(contextProcesses.get(0).getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(contextProcesses.get(0).isVisibleInMenu()).isTrue();
    }

}

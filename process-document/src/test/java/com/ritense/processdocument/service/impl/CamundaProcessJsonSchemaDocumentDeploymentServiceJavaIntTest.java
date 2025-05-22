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

import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.processdocument.BaseIntegrationTest;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@Transactional
class CamundaProcessJsonSchemaDocumentDeploymentServiceJavaIntTest extends BaseIntegrationTest {

    private static final CaseDefinitionId CASE_DEFINITION_ID = new CaseDefinitionId("house", "1.0.0");

    @Autowired
    private DocumentDefinitionService documentDefinitionService;

    @Test
    void shouldDeployProcessDocumentLinkFromResourceFolder() {
        final var processDefinitionCaseDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(new CaseDefinitionId("notahouse", "1.0.0"))
        );

        assertThat(processDefinitionCaseDefinitions.size()).isGreaterThanOrEqualTo(1);
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getProcessDefinitionId()).isNotNull();
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getCaseDefinitionId().getKey()).isEqualTo("notahouse");
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getCaseDefinitionId().getVersionTag().toString()).isEqualTo("1.0.0");
        assertThat(processDefinitionCaseDefinitions.get(0).getCanInitializeDocument()).isTrue();
        assertThat(processDefinitionCaseDefinitions.get(0).getStartableByUser()).isTrue();
    }

    @Test
    public void findProcessDocumentDefinitionWithStartableByUserTrue() {
        Boolean startableByUser = true;
        final var processDefinitionCaseDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(
                CASE_DEFINITION_ID,
                startableByUser,
                null
            )
        );

        assertThat(processDefinitionCaseDefinitions.size()).isGreaterThanOrEqualTo(1);
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getProcessDefinitionId()).isNotNull();
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getCaseDefinitionId().getKey()).isEqualTo("house");
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getCaseDefinitionId().getVersionTag().toString()).isEqualTo("1.0.0");
        assertThat(processDefinitionCaseDefinitions.get(0).getStartableByUser()).isTrue();
    }

    @Test
    public void findProcessDocumentDefinitionWithStartableByUserFalse() {
        Boolean startableByUser = false;
        final var processDefinitionCaseDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(
                new CaseDefinitionId("notahouse", "1.0.0"),
                startableByUser,
                null
            )
        );

        assertThat(processDefinitionCaseDefinitions.size()).isEqualTo(0);
    }

    @Test
    public void findProcessDocumentDefinitionWithCanInitializeDocumentTrue() {
        Boolean canInitializeDocument = true;
        final var processDefinitionCaseDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(
                CASE_DEFINITION_ID,
                null,
                canInitializeDocument
            )
        );

        assertThat(processDefinitionCaseDefinitions.size()).isGreaterThanOrEqualTo(1);
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getProcessDefinitionId()).isNotNull();
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getCaseDefinitionId().getKey()).isEqualTo("house");
        assertThat(processDefinitionCaseDefinitions.get(0).getId().getCaseDefinitionId().getVersionTag().toString()).isEqualTo("1.0.0");
        assertThat(processDefinitionCaseDefinitions.get(0).getCanInitializeDocument()).isTrue();
    }

    @Test
    public void findProcessDocumentDefinitionWithCanInitializeDocumentFalse() {
        Boolean canInitializeDocument = false;
        final var processDefinitionCaseDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(
                new CaseDefinitionId("notahouse", "1.0.0"),
                null,
                canInitializeDocument
            )
        );

        assertThat(processDefinitionCaseDefinitions.size()).isEqualTo(0);
    }
}

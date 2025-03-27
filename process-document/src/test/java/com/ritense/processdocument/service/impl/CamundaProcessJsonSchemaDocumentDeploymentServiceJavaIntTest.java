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
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionId;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@Transactional
class CamundaProcessJsonSchemaDocumentDeploymentServiceJavaIntTest extends BaseIntegrationTest {

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final String PROCESS_DEFINITION_KEY = "loan-process-demo";

    @Autowired
    private DocumentDefinitionService documentDefinitionService;

    @Test
    void shouldDeployProcessDocumentLinkFromResourceFolder() {
        final var processDocumentDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinitions(
                DOCUMENT_DEFINITION_NAME
            )
        );

        assertThat(processDocumentDefinitions.size()).isGreaterThanOrEqualTo(1);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().processDefinitionKey()).hasToString(PROCESS_DEFINITION_KEY);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().documentDefinitionId().name()).isEqualTo(DOCUMENT_DEFINITION_NAME);
        assertThat(processDocumentDefinitions.get(0).canInitializeDocument()).isTrue();
        assertThat(processDocumentDefinitions.get(0).startableByUser()).isTrue();
    }

    @Test
    public void findProcessDocumentDefinitionWithStartableByUserTrue() {
        Boolean startableByUser = true;
        final var processDocumentDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinitions(
                DOCUMENT_DEFINITION_NAME, startableByUser
            )
        );

        assertThat(processDocumentDefinitions.size()).isGreaterThanOrEqualTo(1);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().processDefinitionKey().toString()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().documentDefinitionId().name()).isEqualTo(DOCUMENT_DEFINITION_NAME);
        assertThat(processDocumentDefinitions.get(0).startableByUser()).isTrue();
    }

    @Test
    public void findProcessDocumentDefinitionWithStartableByUserFalse() {
        Boolean startableByUser = false;
        final var processDocumentDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinitions(
                DOCUMENT_DEFINITION_NAME, startableByUser
            )
        );

        assertThat(processDocumentDefinitions.size()).isEqualTo(0);
    }

    @Test
    public void findProcessDocumentDefinitionWithCanInitializeDocumentTrue() {
        Boolean canInitializeDocument = true;
        final var processDocumentDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinitions(
                DOCUMENT_DEFINITION_NAME, null, canInitializeDocument
            )
        );

        assertThat(processDocumentDefinitions.size()).isGreaterThanOrEqualTo(1);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().processDefinitionKey().toString()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().documentDefinitionId().name()).isEqualTo(DOCUMENT_DEFINITION_NAME);
        assertThat(processDocumentDefinitions.get(0).canInitializeDocument()).isTrue();
    }

    @Test
    public void findProcessDocumentDefinitionWithCanInitializeDocumentFalse() {
        Boolean canInitializeDocument = false;
        final var processDocumentDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinitions(
                DOCUMENT_DEFINITION_NAME, null, canInitializeDocument
            )
        );

        assertThat(processDocumentDefinitions.size()).isEqualTo(0);
    }

    @Test
    void shouldCopyProcessDocumentLinkToLatestVersino() {
        var result = AuthorizationContext.runWithoutAuthorization(() -> {
            documentDefinitionService.deploy(
                "{\n" +
                "    \"$id\": \"test.schema\",\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"title\": \"Test\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"housenumber\": {\n" +
                "            \"description\": \"house number must be equal to or greater than zero.\",\n" +
                "            \"type\": \"integer\",\n" +
                "            \"minimum\": 0\n" +
                "        }\n" +
                "    }\n" +
                "}"
            );

            camundaProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(new ProcessDocumentDefinitionRequest(
                "deadlock-process",
                "test",
                true
            ));

            return documentDefinitionService.deploy(
                "{\n" +
                "    \"$id\": \"test.schema\",\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"title\": \"Test changed\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"housenumber\": {\n" +
                "            \"description\": \"house number must be equal to or greater than zero.\",\n" +
                "            \"type\": \"integer\",\n" +
                "            \"minimum\": 0\n" +
                "        }\n" +
                "    }\n" +
                "}"
            );
        });

        var association = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinition(
                new CamundaProcessDefinitionId("deadlock-process")
            ).orElseThrow()
        );

        assertThat(result.errors()).isEmpty();
        assertThat(result.documentDefinition().id().version()).isEqualTo(2);
        assertThat(association.processDocumentDefinitionId().documentDefinitionId()).isEqualTo(result.documentDefinition().id());
    }

}

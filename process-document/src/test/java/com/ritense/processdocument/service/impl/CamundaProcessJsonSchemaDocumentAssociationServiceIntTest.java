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
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.processdocument.BaseIntegrationTest;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import com.ritense.valtimo.repository.camunda.dto.TaskInstanceWithIdentityLink;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CamundaProcessJsonSchemaDocumentAssociationServiceIntTest extends BaseIntegrationTest {

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final String DOCUMENT_DEFINITION_NAME2 = "notahouse";
    private static final String PROCESS_DEFINITION_KEY = "loan-process-demo";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private DocumentDefinitionService documentDefinitionService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private DocumentDefinition oldDocumentDefinition;
    private DocumentDefinition newDocumentDefinition;

    private CaseDefinitionId caseDefinitionId = new CaseDefinitionId("house", "1.0.0");

    @BeforeAll
    public void setup() {
        runWithoutAuthorization(() -> {
            String oldDocumentDefinitionVersion = """
                {
                    "$id": "some-test.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "title": "some-test",
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "string"
                        }
                    }
                }
            """;
            oldDocumentDefinition = documentDefinitionService.deploy(oldDocumentDefinitionVersion, caseDefinitionId).documentDefinition();

            String newDocumentDefinitionVersion = """
                {
                    "$id": "some-test.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "title": "some-test",
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "integer"
                        }
                    }
                }
            """;
            newDocumentDefinition = documentDefinitionService.deploy(newDocumentDefinitionVersion, caseDefinitionId).documentDefinition();

            return null;
        });
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldStartMainProcessAndAssociateCallActivityCalledProcess() throws JsonProcessingException {
        String processDocumentDefinitionKey = "call-activity-subprocess-example";

        final JsonNode jsonContent = objectMapper.readTree("{\"street\": \"Funenparks\"}");
        var newDocumentRequest = new NewDocumentRequest(
            DOCUMENT_DEFINITION_NAME,
            jsonContent
        );
        var newDocumentRequest2 = new NewDocumentRequest(
            DOCUMENT_DEFINITION_NAME2,
            jsonContent
        );
        runWithoutAuthorization(() -> {
            var request = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest);
            var request2 = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest2);

            final NewDocumentAndStartProcessResult newDocumentAndStartProcessResult = camundaProcessJsonSchemaDocumentService
                .newDocumentAndStartProcess(request);

            camundaProcessJsonSchemaDocumentService.newDocumentAndStartProcess(request2);

            final List<TaskInstanceWithIdentityLink> processInstanceTasks = camundaTaskService.getProcessInstanceTasks(
                newDocumentAndStartProcessResult.resultingProcessInstanceId().orElseThrow().toString(),
                newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id().toString()
            );

            final List<CamundaProcessJsonSchemaDocumentInstance> processDocumentInstancesBeforeComplete =
                runWithoutAuthorization(() -> camundaProcessJsonSchemaDocumentAssociationService
                    .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id()));

            assertThat(processDocumentInstancesBeforeComplete).hasSize(1);
            assertThat(processDocumentInstancesBeforeComplete.get(0).isActive()).isEqualTo(true);

            final Document document = newDocumentAndStartProcessResult.resultingDocument().orElseThrow();

            final JsonNode jsonDataUpdate = objectMapper.readTree("{\"street\": \"Funenparks\"}");
            var modifyRequest = new ModifyDocumentAndCompleteTaskRequest(
                new ModifyDocumentRequest(
                    document.id().toString(),
                    jsonDataUpdate
                ),
                processInstanceTasks.iterator().next().getTaskDto().getId()
            );

            final ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTaskResult = camundaProcessJsonSchemaDocumentService
                .modifyDocumentAndCompleteTask(modifyRequest);

            assertThat(modifyDocumentAndCompleteTaskResult.errors()).isEmpty();
            final List<CamundaProcessJsonSchemaDocumentInstance> processDocumentInstances =
                runWithoutAuthorization(() -> camundaProcessJsonSchemaDocumentAssociationService
                    .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id()));
            assertThat(processDocumentInstances).hasSize(2);
            assertThat(processDocumentInstances.get(0).isActive()).isEqualTo(false);
            assertThat(processDocumentInstances.get(1).isActive()).isEqualTo(false);
            return null;
        });
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldStartMainProcessAndNotAssociateSubProcess() throws JsonProcessingException {
        String processDocumentDefinitionKey = "embedded-subprocess-example";

        runWithoutAuthorization(() -> {
            
            final JsonNode jsonContent = objectMapper.readTree("{\"street\": \"Funenparks\"}");
            var newDocumentRequest = new NewDocumentRequest(
                DOCUMENT_DEFINITION_NAME,
                jsonContent
            );
            var request = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest);

            final NewDocumentAndStartProcessResult newDocumentAndStartProcessResult = camundaProcessJsonSchemaDocumentService
                .newDocumentAndStartProcess(request);


            final List<TaskInstanceWithIdentityLink> processInstanceTasks = camundaTaskService.getProcessInstanceTasks(
                newDocumentAndStartProcessResult.resultingProcessInstanceId().orElseThrow().toString(),
                newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id().toString()
            );

            final Document document = newDocumentAndStartProcessResult.resultingDocument().orElseThrow();

            final JsonNode jsonDataUpdate = objectMapper.readTree("{\"street\": \"Funenparks\"}");
            var modifyRequest = new ModifyDocumentAndCompleteTaskRequest(
                new ModifyDocumentRequest(
                    document.id().toString(),
                    jsonDataUpdate
                ),
                processInstanceTasks.iterator().next().getTaskDto().getId()
            );

            final ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTaskResult = camundaProcessJsonSchemaDocumentService
                .modifyDocumentAndCompleteTask(modifyRequest);

            assertThat(modifyDocumentAndCompleteTaskResult.errors()).isEmpty();
            final List<CamundaProcessJsonSchemaDocumentInstance> processDocumentInstances =
                runWithoutAuthorization(() -> camundaProcessJsonSchemaDocumentAssociationService
                    .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id()));
            assertThat(processDocumentInstances).hasSize(1);
            return null;
        });
    }

}

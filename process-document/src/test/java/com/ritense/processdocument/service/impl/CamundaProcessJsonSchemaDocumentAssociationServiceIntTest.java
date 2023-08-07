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

package com.ritense.processdocument.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.Mapper;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.processdocument.BaseIntegrationTest;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import com.ritense.valtimo.repository.camunda.dto.TaskInstanceWithIdentityLink;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
@Transactional
class CamundaProcessJsonSchemaDocumentAssociationServiceIntTest extends BaseIntegrationTest {

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final String PROCESS_DEFINITION_KEY = "loan-process-demo";

    @Inject
    private RuntimeService runtimeService;

    @Test
    public void findProcessDocumentDefinition() {
        final var processDocumentDefinitions = camundaProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentDefinitions(DOCUMENT_DEFINITION_NAME);

        assertThat(processDocumentDefinitions.size()).isEqualTo(1);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().processDefinitionKey().toString()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().documentDefinitionId().name()).isEqualTo(DOCUMENT_DEFINITION_NAME);
    }

    @Test
    public void shouldCreateProcessDocumentDefinition() {
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            DOCUMENT_DEFINITION_NAME,
            true,
            true
        );

        final var optionalProcessDocumentDefinition = camundaProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request);

        assertThat(optionalProcessDocumentDefinition).isPresent();
    }

    @Test
    public void shouldNotCreateProcessDocumentDefinitionForMultipleDossiers() {
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            DOCUMENT_DEFINITION_NAME,
            true,
            true
        );
        camundaProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(request);
        assertThrows(IllegalStateException.class, () -> camundaProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(request));
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldStartMainProcessAndAssociateCallActivityCalledProcess() throws JsonProcessingException {
        String processDocumentDefinitionKey = "call-activity-subprocess-example";

        final var processDocumentRequest = new ProcessDocumentDefinitionRequest(
            processDocumentDefinitionKey,
            DOCUMENT_DEFINITION_NAME,
            true,
            true
        );
        camundaProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(processDocumentRequest);

        final JsonNode jsonContent = Mapper.INSTANCE.get().readTree("{\"street\": \"Funenparks\"}");
        var newDocumentRequest = new NewDocumentRequest(
            DOCUMENT_DEFINITION_NAME,
            jsonContent,
            "1"
        );
        var request = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest);

        final NewDocumentAndStartProcessResult newDocumentAndStartProcessResult = camundaProcessJsonSchemaDocumentService
            .newDocumentAndStartProcess(request);


        final List<TaskInstanceWithIdentityLink> processInstanceTasks = camundaTaskService.getProcessInstanceTasks(
            newDocumentAndStartProcessResult.resultingProcessInstanceId().orElseThrow().toString(),
            newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id().toString()
        );

        final List<CamundaProcessJsonSchemaDocumentInstance> processDocumentInstancesBeforeComplete = camundaProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id());

        assertThat(processDocumentInstancesBeforeComplete).hasSize(1);
        assertThat(processDocumentInstancesBeforeComplete.get(0).isActive()).isEqualTo(true);

        final Document document = newDocumentAndStartProcessResult.resultingDocument().orElseThrow();

        final JsonNode jsonDataUpdate = Mapper.INSTANCE.get().readTree("{\"street\": \"Funenparks\"}");
        var modifyRequest = new ModifyDocumentAndCompleteTaskRequest(
            new ModifyDocumentRequest(
                document.id().toString(),
                jsonDataUpdate,
                document.version().toString()
            ),
            processInstanceTasks.iterator().next().getTaskDto().getId()
        );

        final ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTaskResult = camundaProcessJsonSchemaDocumentService
            .modifyDocumentAndCompleteTask(modifyRequest);

        assertThat(modifyDocumentAndCompleteTaskResult.errors()).isEmpty();
        final List<CamundaProcessJsonSchemaDocumentInstance> processDocumentInstances = camundaProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id());
        assertThat(processDocumentInstances).hasSize(2);
        assertThat(processDocumentInstances.get(0).isActive()).isEqualTo(false);
        assertThat(processDocumentInstances.get(1).isActive()).isEqualTo(false);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldStartMainProcessAndNotAssociateSubProcess() throws JsonProcessingException {
        String processDocumentDefinitionKey = "embedded-subprocess-example";

        final var processDocumentRequest = new ProcessDocumentDefinitionRequest(
            processDocumentDefinitionKey,
            DOCUMENT_DEFINITION_NAME,
            true,
            true
        );
        camundaProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(processDocumentRequest);

        final JsonNode jsonContent = Mapper.INSTANCE.get().readTree("{\"street\": \"Funenparks\"}");
        var newDocumentRequest = new NewDocumentRequest(
            DOCUMENT_DEFINITION_NAME,
            jsonContent,
            "1"
        );
        var request = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest);

        final NewDocumentAndStartProcessResult newDocumentAndStartProcessResult = camundaProcessJsonSchemaDocumentService
            .newDocumentAndStartProcess(request);


        final List<TaskInstanceWithIdentityLink> processInstanceTasks = camundaTaskService.getProcessInstanceTasks(
            newDocumentAndStartProcessResult.resultingProcessInstanceId().orElseThrow().toString(),
            newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id().toString()
        );

        final Document document = newDocumentAndStartProcessResult.resultingDocument().orElseThrow();

        final JsonNode jsonDataUpdate = Mapper.INSTANCE.get().readTree("{\"street\": \"Funenparks\"}");
        var modifyRequest = new ModifyDocumentAndCompleteTaskRequest(
            new ModifyDocumentRequest(
                document.id().toString(),
                jsonDataUpdate,
                document.version().toString()
            ),
            processInstanceTasks.iterator().next().getTaskDto().getId()
        );

        final ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTaskResult = camundaProcessJsonSchemaDocumentService
            .modifyDocumentAndCompleteTask(modifyRequest);

        assertThat(modifyDocumentAndCompleteTaskResult.errors()).isEmpty();
        final List<CamundaProcessJsonSchemaDocumentInstance> processDocumentInstances = camundaProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id());
        assertThat(processDocumentInstances).hasSize(1);
    }

}

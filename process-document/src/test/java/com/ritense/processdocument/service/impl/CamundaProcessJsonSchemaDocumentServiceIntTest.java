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
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.processdocument.BaseIntegrationTest;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionId;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.valtimo.service.CamundaProcessService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@Transactional
class CamundaProcessJsonSchemaDocumentServiceIntTest extends BaseIntegrationTest {

    @Autowired
    protected CamundaProcessService camundaProcessService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final String PROCESS_DEFINITION_KEY = "unassociated-process";

    @Test
    void shouldNewDocumentAndStartProcessForUnassociatedProcess() throws JsonProcessingException {
        var startRequest = new NewDocumentAndStartProcessRequest(
            PROCESS_DEFINITION_KEY,
            new NewDocumentRequest(DOCUMENT_DEFINITION_NAME, objectMapper.readTree("{}"))
        );
        var result = runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentService.newDocumentAndStartProcess(startRequest)
        );

        var optAssociation = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinition(new CamundaProcessDefinitionId(PROCESS_DEFINITION_KEY))
        );
        assertThat(optAssociation).isEmpty();
        assertThat(result.errors()).isEmpty();
        assertThat(result.resultingDocument()).isPresent();
        assertThat(result.resultingProcessInstanceId()).isPresent();
    }

}

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

package com.ritense.processdocument.service;

import com.ritense.document.domain.Document;
import com.ritense.processdocument.domain.ProcessDefinitionKey;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.ProcessDocumentInstance;
import com.ritense.processdocument.domain.ProcessDocumentInstanceId;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessDocumentAssociationService {

    Optional<? extends ProcessDocumentDefinition> findProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey);

    ProcessDocumentDefinition getProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey);

    List<? extends ProcessDocumentDefinition> findAllProcessDocumentDefinitions(ProcessDefinitionKey processDefinitionKey);

    Optional<? extends ProcessDocumentDefinition> findProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey, long documentDefinitionVersion);

    ProcessDocumentDefinition getProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey, long documentDefinitionVersion);

    List<? extends ProcessDocumentDefinition> findProcessDocumentDefinitions(String documentDefinitionName);

    List<? extends ProcessDocumentDefinition> findProcessDocumentDefinitionsByProcessDefinitionKey(String processDefinitionKey);

    Optional<? extends ProcessDocumentDefinition> findByDocumentDefinitionName(String documentDefinitionName);

    Optional<? extends ProcessDocumentInstance> findProcessDocumentInstance(ProcessInstanceId processInstanceId);

    List<? extends ProcessDocumentInstance> findProcessDocumentInstances(Document.Id documentId);

    void deleteProcessDocumentInstances(String processName);

    FunctionResult<? extends ProcessDocumentInstance, OperationError> getProcessDocumentInstanceResult(
        ProcessDocumentInstanceId processDocumentInstanceId
    );

    Optional<? extends ProcessDocumentDefinition> createProcessDocumentDefinition(
        ProcessDocumentDefinitionRequest request
    );

    void deleteProcessDocumentDefinition(ProcessDocumentDefinitionRequest processDocumentDefinitionRequest);

    void deleteProcessDocumentDefinition(String documentDefititionName);

    Optional<? extends ProcessDocumentInstance> createProcessDocumentInstance(
        String processInstanceId,
        UUID documentId,
        String processName
    );

}

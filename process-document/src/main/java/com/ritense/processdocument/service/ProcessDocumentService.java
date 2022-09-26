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

package com.ritense.processdocument.service;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentForRunningProcessRequest;
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest;
import com.ritense.processdocument.domain.request.Request;
import com.ritense.processdocument.service.result.DocumentFunctionResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentForRunningProcessResult;
import com.ritense.processdocument.service.result.StartProcessForDocumentResult;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;

import java.util.Optional;

public interface ProcessDocumentService {

    DocumentFunctionResult<JsonSchemaDocument> dispatch(Request request);

    NewDocumentAndStartProcessResult newDocumentAndStartProcess(NewDocumentAndStartProcessRequest request);

    NewDocumentForRunningProcessResult newDocumentForRunningProcess(NewDocumentForRunningProcessRequest request);

    ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTask(ModifyDocumentAndCompleteTaskRequest request);

    ModifyDocumentAndStartProcessResult modifyDocumentAndStartProcess(ModifyDocumentAndStartProcessRequest request);

    StartProcessForDocumentResult startProcessForDocument(StartProcessForDocumentRequest request);

    Document getDocument(DelegateExecution execution);

    JsonSchemaDocumentId getDocumentId(ProcessInstanceId processInstanceId, VariableScope variableScope);

    Document getDocument(ProcessInstanceId processInstanceId, VariableScope variableScope);

    Optional<? extends ProcessDocumentDefinition> findProcessDocumentDefinition(ProcessInstanceId processInstanceId);
}

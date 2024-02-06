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

package com.ritense.processdocument.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.annotation.RunWithoutAuthorization;
import com.ritense.document.service.DocumentService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.transaction.annotation.Transactional;

public class DeadlockService {

    private DocumentService documentService;
    private ObjectMapper objectMapper;

    public DeadlockService(
        DocumentService documentService,
        ObjectMapper objectMapper
    ) {
        this.documentService = documentService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @RunWithoutAuthorization
    public void updateDocument(DelegateExecution execution) throws JsonProcessingException {
        var threadId = Thread.currentThread().getId();
        var document = documentService.get(execution.getBusinessKey());

        // Create a database-row-lock on the json_schema_document table
        documentService.assignUserToDocument(document.id().getId(), "john@ritense.com");

        // 1. Lock the 'synchronized' function
        // 2. In the 'synchronized' function, create a database-row-lock on the json_schema_document table
        documentService.modifyDocument(document, objectMapper.readTree("{\"street\": \"Thread: " + threadId + "\"}"));
    }
}
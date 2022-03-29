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

package com.ritense.document.web.rest.impl;

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.UndeployDocumentDefinitionService;
import com.ritense.document.service.request.DocumentDefinitionCreateRequest;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.document.service.result.UndeployDocumentDefinitionResult;
import com.ritense.document.web.rest.DocumentDefinitionResource;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.springframework.http.ResponseEntity.ok;

public class JsonSchemaDocumentDefinitionResource implements DocumentDefinitionResource {

    private final DocumentDefinitionService documentDefinitionService;
    private final UndeployDocumentDefinitionService undeployDocumentDefinitionService;

    public JsonSchemaDocumentDefinitionResource(DocumentDefinitionService documentDefinitionService, UndeployDocumentDefinitionService undeployDocumentDefinitionService) {
        this.documentDefinitionService = documentDefinitionService;
        this.undeployDocumentDefinitionService = undeployDocumentDefinitionService;
    }

    @Override
    public ResponseEntity<Page<? extends DocumentDefinition>> getDocumentDefinitions(boolean filteredOnRole, Pageable pageable) {
        return ok(documentDefinitionService.findForUser(filteredOnRole, pageable));
    }

    @Override
    @SneakyThrows
    public ResponseEntity<? extends DocumentDefinition> getDocumentDefinition(String name) {
        if (!documentDefinitionService.currentUserCanAccessDocumentDefinition(true, name)) {
            ResponseEntity.notFound();
        }

        return ResponseEntity.of(documentDefinitionService.findLatestByName(name));
    }

    @Override
    public ResponseEntity<DeployDocumentDefinitionResult> deployDocumentDefinition(DocumentDefinitionCreateRequest request) {
        return applyResult(documentDefinitionService.deploy(request.getDefinition()));
    }

    @Override
    public ResponseEntity<UndeployDocumentDefinitionResult> removeDocumentDefinition(String name) {
        return applyResult(undeployDocumentDefinitionService.undeploy(name));
    }

    @Override
    public ResponseEntity<Set<String>> getDocumentDefinitionRoles(String documentDefinitionName) {
        return ResponseEntity.ok()
            .body(documentDefinitionService.getDocumentDefinitionRoles(documentDefinitionName));
    }

    @Override
    public ResponseEntity<Void> putDocumentDefinitionRoles(String documentDefinitionName, Set<String> roles) {
        documentDefinitionService.putDocumentDefinitionRoles(documentDefinitionName, roles);

        return ResponseEntity.ok().build();
    }

    <T extends DeployDocumentDefinitionResult> ResponseEntity<T> applyResult(T result) {
        var httpStatus = result.documentDefinition() != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }

    <T extends UndeployDocumentDefinitionResult> ResponseEntity<T> applyResult(T result) {
        var httpStatus = result.documentDefinitionName() != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }
}
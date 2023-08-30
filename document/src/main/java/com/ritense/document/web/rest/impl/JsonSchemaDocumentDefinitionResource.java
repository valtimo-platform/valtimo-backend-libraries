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

package com.ritense.document.web.rest.impl;

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.assignee.UnassignedDocumentCountDto;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentStatisticService;
import com.ritense.document.service.UndeployDocumentDefinitionService;
import com.ritense.document.service.request.DocumentDefinitionCreateRequest;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.document.service.result.UndeployDocumentDefinitionResult;
import com.ritense.document.web.rest.DocumentDefinitionResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;
import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static org.springframework.http.ResponseEntity.ok;

public class JsonSchemaDocumentDefinitionResource implements DocumentDefinitionResource {

    private final DocumentDefinitionService documentDefinitionService;
    private final UndeployDocumentDefinitionService undeployDocumentDefinitionService;
    private final DocumentStatisticService documentStatisticService;

    public JsonSchemaDocumentDefinitionResource(
        DocumentDefinitionService documentDefinitionService,
        UndeployDocumentDefinitionService undeployDocumentDefinitionService,
        DocumentStatisticService documentStatisticService
    ) {
        this.documentDefinitionService = documentDefinitionService;
        this.undeployDocumentDefinitionService = undeployDocumentDefinitionService;
        this.documentStatisticService = documentStatisticService;
    }

    @Override
    public ResponseEntity<Page<? extends DocumentDefinition>> getDocumentDefinitions(Pageable pageable) {
        return ok(documentDefinitionService.findAll(fixPageable(pageable)));
    }

    @Override
    public ResponseEntity<Page<? extends DocumentDefinition>> getDocumentDefinitionsForManagement(Pageable pageable) {
        return ok(runWithoutAuthorization(() -> documentDefinitionService.findAllForManagement(fixPageable(pageable))));
    }

    /**
     * This keeps the API backwards compatible with old jpa entity columns in the sort
     * @param pageable
     * @return
     */
    private Pageable fixPageable(Pageable pageable) {
        return PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(
                pageable.getSort()
                    .stream()
                    .map(order ->
                        new Sort.Order(order.getDirection(), mapSortProperty(order.getProperty()))
                    ).collect(Collectors.toList())
            )
        );
    }

    private String mapSortProperty(String property) {
        if (property.equals("id.name")) {
            return "document_definition_name";
        }
        if (property.equals("id.version")) {
            return "document_definition_version";
        }
        if (property.equals("readOnly")) {
            return "read_only";
        }
        if (property.equals("createdOn")) {
            return "created_on";
        }
        return property;
    }

    @Override
    public ResponseEntity<? extends DocumentDefinition> getDocumentDefinition(String name) {
        return ResponseEntity.of(documentDefinitionService.findLatestByName(name));
    }

    @Override
    public ResponseEntity<List<UnassignedDocumentCountDto>> getUnassignedDocumentCount() {
        return ResponseEntity.ok(documentStatisticService.getUnassignedDocumentCountDtos());
    }

    @Override
    public ResponseEntity<DeployDocumentDefinitionResult> deployDocumentDefinition(DocumentDefinitionCreateRequest request) {
        return applyResult(documentDefinitionService.deploy(request.getDefinition()));
    }

    @Override
    public ResponseEntity<UndeployDocumentDefinitionResult> removeDocumentDefinition(String name) {
        return applyResult(undeployDocumentDefinitionService.undeploy(name));
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

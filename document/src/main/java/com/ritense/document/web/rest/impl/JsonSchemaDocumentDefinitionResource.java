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

package com.ritense.document.web.rest.impl;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.of;
import static org.springframework.http.ResponseEntity.ok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.assignee.UnassignedDocumentCountDto;
import com.ritense.document.domain.impl.template.DocumentDefinitionTemplateRequestDto;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentStatisticService;
import com.ritense.document.service.UndeployDocumentDefinitionService;
import com.ritense.document.service.request.DocumentDefinitionCreateRequest;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.document.service.result.DocumentVersionsResult;
import com.ritense.document.service.result.UndeployDocumentDefinitionResult;
import com.ritense.document.web.rest.DocumentDefinitionResource;
import com.ritense.valtimo.contract.json.MapperSingleton;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    public ResponseEntity<Object> getDocumentDefinitionTemplate(DocumentDefinitionTemplateRequestDto requestDto) throws
        JsonProcessingException {
        return ok(MapperSingleton.get().readTree(
            """
                {
                    "$id": "%s.schema",
                    "type": "object",
                    "title": "%s",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "properties": {},
                    "additionalProperties":false
                }
                """.formatted(requestDto.documentDefinitionId(), requestDto.documentDefinitionTitle()))
        );
    }

    @Override
    public ResponseEntity<Page<? extends DocumentDefinition>> getDocumentDefinitionsForManagement(Pageable pageable) {
        return ok(runWithoutAuthorization(() -> documentDefinitionService.findAllForManagement(fixPageable(pageable))));
    }

    @Override
    public ResponseEntity<? extends DocumentDefinition> getDocumentDefinitionForManagement(String name) {
        return of(runWithoutAuthorization(() -> documentDefinitionService.findLatestByName(name)));
    }

    /**
     * This keeps the API backwards compatible with old jpa entity columns in the sort.
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
        return of(documentDefinitionService.findLatestByName(name));
    }

    @Override
    public ResponseEntity<? extends DocumentDefinition> getDocumentDefinitionVersion(String name, long version) {
        return of(runWithoutAuthorization(() -> documentDefinitionService.findByNameAndVersion(name, version)));
    }

    @Override
    public ResponseEntity<DocumentVersionsResult> getDocumentDefinitionVersions(String name) {
        List<Long> versions = runWithoutAuthorization(() -> documentDefinitionService.findVersionsByName(name));

        if (versions.isEmpty()) {
            return notFound().build();
        }

        return ok(new DocumentVersionsResult(name, versions));
    }

    @Override
    public ResponseEntity<List<UnassignedDocumentCountDto>> getUnassignedDocumentCount() {
        return ok(documentStatisticService.getUnassignedDocumentCountDtos());
    }

    @Override
    public ResponseEntity<DeployDocumentDefinitionResult> deployDocumentDefinition(
        DocumentDefinitionCreateRequest request
    ) {
        var result = runWithoutAuthorization(() -> documentDefinitionService.deploy(request.getDefinition()));
        var httpStatus = result.documentDefinition() != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }

    @Override
    public ResponseEntity<UndeployDocumentDefinitionResult> removeDocumentDefinition(String name) {
        var result = runWithoutAuthorization(() -> undeployDocumentDefinitionService.undeploy(name));
        var httpStatus = result.documentDefinitionName() != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }
}

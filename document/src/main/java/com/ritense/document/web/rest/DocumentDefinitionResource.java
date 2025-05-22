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

package com.ritense.document.web.rest;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.assignee.UnassignedDocumentCountDto;
import com.ritense.document.domain.impl.template.DocumentDefinitionTemplateRequestDto;
import com.ritense.document.service.request.DocumentDefinitionCreateRequest;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.document.service.result.DocumentVersionsResult;
import com.ritense.document.service.result.UndeployDocumentDefinitionResult;
import com.ritense.logging.LoggableResource;
import com.ritense.valtimo.contract.annotation.SkipComponentScan;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SkipComponentScan
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public interface DocumentDefinitionResource {

    @GetMapping("/v1/document-definition")
    ResponseEntity<Page<? extends DocumentDefinition>> getDocumentDefinitions(
        @PageableDefault(sort = {"id_name"}, direction = ASC) Pageable pageable
    );

    @PostMapping(value = "/management/v1/document-definition-template", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getDocumentDefinitionTemplate(@Valid @RequestBody DocumentDefinitionTemplateRequestDto requestDto) throws
        JsonProcessingException;

    @GetMapping("/management/v1/document-definition")
    ResponseEntity<Page<? extends DocumentDefinition>> getDocumentDefinitionsForManagement(
        @PageableDefault(sort = {"id_name"}, direction = ASC) Pageable pageable
    );

    @GetMapping("/management/v1/document-definition/{name}")
    ResponseEntity<? extends DocumentDefinition> getDocumentDefinitionForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable String name
    );

    @GetMapping("/v1/document-definition/{name}")
    ResponseEntity<? extends DocumentDefinition> getDocumentDefinition(
        @LoggableResource("documentDefinitionName") @PathVariable String name
    );

    @GetMapping("/management/v1/document-definition/{name}/version/{version}")
    ResponseEntity<? extends DocumentDefinition> getDocumentDefinitionVersion(
        @LoggableResource("documentDefinitionName") @PathVariable String name,
        @PathVariable long version
    );

    @GetMapping("/management/v1/document-definition/{name}/version")
    ResponseEntity<DocumentVersionsResult> getDocumentDefinitionVersions(
        @LoggableResource("documentDefinitionName") @PathVariable String name
    );

    @GetMapping("/v1/document-definition/open/count")
    ResponseEntity<List<UnassignedDocumentCountDto>> getUnassignedDocumentCount();

    @PostMapping(value = {
        "/v1/document-definition", //Deprecated since v11
        "/management/v1/document-definition"
    }, consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<DeployDocumentDefinitionResult> deployDocumentDefinition(
        @Valid @RequestBody DocumentDefinitionCreateRequest request
    );

    @DeleteMapping(value = {
        "/v1/document-definition/{name}", //Deprecated since v11
        "/management/v1/document-definition/{name}"
    })
    ResponseEntity<UndeployDocumentDefinitionResult> removeDocumentDefinition(
        @LoggableResource("documentDefinitionName") @PathVariable String name
    );
}

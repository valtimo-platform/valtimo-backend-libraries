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

package com.ritense.document.web.rest;

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.assignee.UnassignedDocumentCountDto;
import com.ritense.document.service.request.DocumentDefinitionCreateRequest;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.document.service.result.UndeployDocumentDefinitionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public interface DocumentDefinitionResource {

    @GetMapping(value = "/document-definition")
    ResponseEntity<Page<? extends DocumentDefinition>> getDocumentDefinitions(
        @RequestParam(name = "filteredOnRole", defaultValue = "true") boolean filteredOnRole,
        @PageableDefault(sort = {"document_definition_name"}, direction = ASC) Pageable pageable
    );

    @GetMapping(value = "/document-definition/{name}")
    ResponseEntity<? extends DocumentDefinition> getDocumentDefinition(@PathVariable String name);

    @GetMapping(value = "/document-definition/open/count")
    ResponseEntity<List<UnassignedDocumentCountDto>> getUnassignedDocumentCount();

    @PostMapping(value = "/document-definition", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<DeployDocumentDefinitionResult> deployDocumentDefinition(
        @Valid @RequestBody DocumentDefinitionCreateRequest request
    );

    @DeleteMapping(value = "/document-definition/{name}")
    ResponseEntity<UndeployDocumentDefinitionResult> removeDocumentDefinition(@PathVariable String name);

    @GetMapping(value = "/document-definition/{documentDefinitionName}/roles")
    ResponseEntity<Set<String>> getDocumentDefinitionRoles(@PathVariable String documentDefinitionName);

    @PutMapping(value = "/document-definition/{documentDefinitionName}/roles")
    ResponseEntity<Void> putDocumentDefinitionRoles(@PathVariable String documentDefinitionName, @RequestBody Set<String> roles);
}

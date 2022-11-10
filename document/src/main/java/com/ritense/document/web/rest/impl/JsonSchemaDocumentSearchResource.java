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

import com.ritense.document.domain.Document;
import com.ritense.document.domain.search.SearchRequestValidator;
import com.ritense.document.domain.search.SearchWithConfigRequest;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.impl.SearchRequest;
import com.ritense.document.web.rest.DocumentSearchResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class JsonSchemaDocumentSearchResource implements DocumentSearchResource {

    private final DocumentSearchService documentSearchService;

    public JsonSchemaDocumentSearchResource(DocumentSearchService documentSearchService) {
        this.documentSearchService = documentSearchService;
    }

    @Override
    @PostMapping(value = "/document-search")
    public ResponseEntity<Page<? extends Document>> search(
        @RequestBody SearchRequest searchRequest,
        @PageableDefault(sort = {"createdOn"}, direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            documentSearchService.search(searchRequest, pageable)
        );
    }

    @Override
    @PostMapping(value = "/v1/document-definition/{name}/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<? extends Document>> search(
        @PathVariable(name = "name") String documentDefinitionName,
        @RequestBody SearchWithConfigRequest searchRequest,
        @PageableDefault(sort = {"createdOn"}, direction = DESC) Pageable pageable
    ) {
        SearchRequestValidator.validate(searchRequest);
        return ResponseEntity.ok(
            documentSearchService.search(documentDefinitionName, searchRequest, pageable)
        );
    }

}

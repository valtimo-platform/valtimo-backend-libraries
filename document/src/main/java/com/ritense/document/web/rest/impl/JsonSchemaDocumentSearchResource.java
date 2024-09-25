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

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.search.SearchRequestValidator;
import com.ritense.document.domain.search.SearchWithConfigRequest;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.impl.SearchRequest;
import com.ritense.document.web.rest.DocumentSearchResource;
import com.ritense.logging.LoggableResource;
import com.ritense.valtimo.contract.annotation.SkipComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SkipComponentScan
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class JsonSchemaDocumentSearchResource implements DocumentSearchResource {

    private final DocumentSearchService documentSearchService;

    public JsonSchemaDocumentSearchResource(DocumentSearchService documentSearchService) {
        this.documentSearchService = documentSearchService;
    }

    @Override
    @PostMapping("/v1/document-search")
    public ResponseEntity<Page<? extends Document>> search(
        @RequestBody SearchRequest searchRequest,
        @PageableDefault(sort = {"createdOn"}, direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            documentSearchService.search(searchRequest, pageable)
        );
    }

    @Override
    @PostMapping("/v1/document-definition/{name}/search")
    public ResponseEntity<Page<? extends Document>> search(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "name") String documentDefinitionName,
        @RequestBody SearchWithConfigRequest searchRequest,
        @PageableDefault(sort = {"createdOn"}, direction = DESC) Pageable pageable
    ) {
        SearchRequestValidator.validate(searchRequest);
        return ResponseEntity.ok(
            documentSearchService.search(documentDefinitionName, searchRequest, pageable)
        );
    }

}

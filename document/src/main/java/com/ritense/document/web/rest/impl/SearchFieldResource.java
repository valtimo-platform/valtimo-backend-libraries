/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.service.SearchFieldService;
import com.ritense.document.web.rest.DocumentSearchFields;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchFieldResource implements DocumentSearchFields {

    private final SearchFieldService searchFieldService;

    public SearchFieldResource(final SearchFieldService searchFieldService) {
        this.searchFieldService = searchFieldService;
    }

    @Override
    @PostMapping("/v1/document-search/{documentDefinitionName}/fields")
    public ResponseEntity<Void> addSearchField(
        @PathVariable String documentDefinitionName,
        @RequestBody SearchField searchField) {

        searchFieldService.addSearchField(documentDefinitionName, searchField);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/v1/document-search/{documentDefinitionName}/fields")
    public ResponseEntity<List<SearchField>> getSearchField(
        @PathVariable String documentDefinitionName) {
        return ResponseEntity.ok(searchFieldService.getSearchFields(documentDefinitionName));
    }

    @Override
    @PutMapping("/v1/document-search/{documentDefinitionName}/fields")
    public ResponseEntity<List<SearchField>> updateSearchField(
            @RequestBody SearchField searchField) {
        if(searchField.getId() == null){
            return ResponseEntity.badRequest().build();
        }
        searchFieldService.updateSearchFields(searchField);
        return ResponseEntity.ok().build();
    }
}

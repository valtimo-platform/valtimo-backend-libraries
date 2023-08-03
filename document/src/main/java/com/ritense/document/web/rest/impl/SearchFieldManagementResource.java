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

import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.impl.searchfield.SearchFieldDto;
import com.ritense.document.service.SearchFieldService;
import com.ritense.document.web.rest.DocumentSearchFieldsManagement;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RequestMapping(value = "/api/management", produces = APPLICATION_JSON_UTF8_VALUE)
public class SearchFieldManagementResource implements DocumentSearchFieldsManagement {

    private final SearchFieldService searchFieldService;

    public SearchFieldManagementResource(final SearchFieldService searchFieldService) {
        this.searchFieldService = searchFieldService;
    }

    @Override
    @GetMapping("/v1/document-search/{documentDefinitionName}/fields")
    public ResponseEntity<List<SearchFieldDto>> getAdminSearchFields(
        @PathVariable String documentDefinitionName) {
        return AuthorizationContext.runWithoutAuthorization(() -> ResponseEntity.ok(SearchFieldMapper
                .toDtoList(searchFieldService.getSearchFields(documentDefinitionName))));
    }
}

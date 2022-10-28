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

package com.ritense.document.web.rest;

import com.ritense.document.domain.impl.searchfield.SearchFieldDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DocumentSearchFields {

    ResponseEntity<Void> addSearchField(
            @PathVariable String documentDefinitionName,
            @RequestBody SearchFieldDto searchField);
    ResponseEntity<List<SearchFieldDto>> getSearchField(String documentDefinitionName);
    ResponseEntity<Void> updateSearchField(
            @PathVariable String documentDefinitionName,
            @RequestBody SearchFieldDto searchFieldDto);
    ResponseEntity<Void> deleteSearchField(
            @PathVariable String documentDefinitionName,
            @RequestParam String key);
}

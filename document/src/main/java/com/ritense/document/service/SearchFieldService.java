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

package com.ritense.document.service;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDto;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import com.ritense.document.repository.SearchFieldRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFieldService {

    private final SearchFieldRepository searchFieldRepository;

    public SearchFieldService(final SearchFieldRepository searchFieldRepository) {
        this.searchFieldRepository = searchFieldRepository;
    }

    public void addSearchField(String documentDefinitionName, SearchField searchField) {
        Optional<SearchField> optSearchField = searchFieldRepository
                .findByIdDocumentDefinitionNameAndKey(documentDefinitionName, searchField.getKey());
        if (optSearchField.isPresent()) {
            throw new IllegalArgumentException("Search field already exists for document '" + documentDefinitionName + "' and key '" + searchField.getKey() + "'.");
        }
        SearchFieldId searchFieldId = SearchFieldId.newId(documentDefinitionName);
        searchField.setId(searchFieldId);
        searchFieldRepository.save(searchField);
    }

    public List<SearchField> getSearchFields(String documentDefinitionName) {
        return searchFieldRepository.findAllByIdDocumentDefinitionName(documentDefinitionName);
    }

    public void updateSearchFields(String documentDefinitionName, SearchFieldDto searchFieldDto) {
        Optional<SearchField> fieldToUpdate = searchFieldRepository
                .findByIdDocumentDefinitionNameAndKey(documentDefinitionName, searchFieldDto.getKey());
        if (fieldToUpdate.isEmpty()) {
            throw new IllegalArgumentException("No search field found for document '" + documentDefinitionName + "' and key '" + searchFieldDto.getKey() + "'.");
        }
        fieldToUpdate.ifPresent(searchField -> {
            searchField.setPath(searchFieldDto.getPath());
            searchField.setDatatype(searchFieldDto.getDatatype());
            searchField.setFieldtype(searchFieldDto.getFieldtype());
            searchField.setMatchtype(searchFieldDto.getMatchtype());
            searchFieldRepository.save(searchField);
        });
    }

    public void createSearchConfiguration(List<SearchField> searchFields) {
        if (searchFields.stream()
                        .filter((searchField ->
                                Collections.frequency(searchFields.stream()
                                        .flatMap(field -> Stream.of(field.getKey()))
                                        .collect(Collectors.toList()), searchField.getKey()
                                ) > 1))
                        .distinct().findAny().isEmpty()) {
            searchFieldRepository.saveAll(searchFields);
        }
    }

    public void deleteSearchField(String documentDefinitionName, String key) {
        searchFieldRepository.findByIdDocumentDefinitionNameAndKey(documentDefinitionName, key).ifPresent(
                searchFieldRepository::delete);
    }
}

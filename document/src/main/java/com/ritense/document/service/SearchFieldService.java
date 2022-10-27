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
import com.ritense.document.web.rest.impl.SearchFieldMapper;

import java.util.List;
import java.util.Optional;

public class SearchFieldService {

    private final SearchFieldRepository searchFieldRepository;

    public SearchFieldService(final SearchFieldRepository searchFieldRepository) {
        this.searchFieldRepository = searchFieldRepository;
    }

    public void addSearchField(String documentDefinitionName, SearchField searchField) {
        SearchFieldId searchFieldId = SearchFieldId.newId(documentDefinitionName);
        searchField.setId(searchFieldId);
        searchFieldRepository.save(searchField);
    }

    public List<SearchFieldDto> getSearchFields(String documentDefinitionName) {
        return SearchFieldMapper.toDtoList(searchFieldRepository.findAllByIdDocumentDefinitionName(documentDefinitionName));
    }

    public void updateSearchFields(String documentDefinitionName,SearchFieldDto searchFieldDto) {
        Optional<SearchField> fieldToUpdate = searchFieldRepository.findByIdDocumentDefinitionNameAndKey(documentDefinitionName,searchFieldDto.getKey());
        fieldToUpdate.ifPresent(searchFieldRepository::save);
    }
}

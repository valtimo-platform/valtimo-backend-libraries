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

package com.ritense.document.service;

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.EntityAuthorizationRequest;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDataType;
import com.ritense.document.domain.impl.searchfield.SearchFieldDto;
import com.ritense.document.domain.impl.searchfield.SearchFieldFieldType;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import com.ritense.document.domain.impl.searchfield.SearchFieldMatchType;
import com.ritense.document.exception.InvalidSearchFieldException;
import com.ritense.document.repository.SearchFieldRepository;
import com.ritense.document.web.rest.impl.SearchFieldMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.Status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ritense.document.repository.SearchFieldRepository.byIdDocumentDefinitionName;
import static com.ritense.document.service.SearchFieldActionProvider.LIST_VIEW;

public class SearchFieldService {

    private final SearchFieldRepository searchFieldRepository;
    private final DocumentDefinitionService documentDefinitionService;
    private final AuthorizationService authorizationService;

    public SearchFieldService(
            final SearchFieldRepository searchFieldRepository,
            final DocumentDefinitionService documentDefinitionService,
            final AuthorizationService authorizationService
    ) {
        this.searchFieldRepository = searchFieldRepository;
        this.documentDefinitionService = documentDefinitionService;
        this.authorizationService = authorizationService;
    }

    public void addSearchField(String documentDefinitionName, SearchField searchField) {
        denyAuthorization();

        Optional<SearchField> optSearchField = searchFieldRepository
                .findByIdDocumentDefinitionNameAndKey(documentDefinitionName, searchField.getKey());
        if (optSearchField.isPresent()) {
            throw new IllegalArgumentException("Search field already exists for document '" + documentDefinitionName + "' and key '" + searchField.getKey() + "'.");
        }
        validateSearchField(SearchFieldMapper.toDto(searchField));
        SearchFieldId searchFieldId = SearchFieldId.newId(documentDefinitionName);
        searchField.setId(searchFieldId);
        documentDefinitionService.validateJsonPath(documentDefinitionName, searchField.getPath());
        searchFieldRepository.save(searchField);
    }

    public List<SearchField> getSearchFields(String documentDefinitionName) {
        Specification<SearchField> authorizationSpec = authorizationService.getAuthorizationSpecification(
            new EntityAuthorizationRequest<>(
                SearchField.class,
                LIST_VIEW,
                null
            ),
            null
        );

        return searchFieldRepository.findAll(
            authorizationSpec.and(byIdDocumentDefinitionName(documentDefinitionName)),
            Sort.by(Sort.Order.asc("order"))
        );
    }

    public void updateSearchFields(String documentDefinitionName, List<SearchFieldDto> searchFieldDtos) {
        denyAuthorization();

        searchFieldDtos.forEach(this::validateSearchField);
        searchFieldDtos.forEach(searchFieldDto ->
                documentDefinitionService.validateJsonPath(documentDefinitionName, searchFieldDto.getPath())
        );
        var searchFields = IntStream.range(0, searchFieldDtos.size())
                .mapToObj(index -> toOrderedSearchField(documentDefinitionName, searchFieldDtos.get(index), index))
                .toList();
        searchFieldRepository.saveAll(searchFields);
    }

    public void createSearchConfiguration(List<SearchField> searchFields) {
        denyAuthorization();

        searchFields.forEach(searchField -> {
            assert searchField.getId() != null;
            documentDefinitionService.validateJsonPath(searchField.getId().getDocumentDefinitionName(), searchField.getPath());
        });
        if (searchFields.stream()
                .filter((searchField ->
                        Collections.frequency(searchFields.stream()
                                .flatMap(field -> Stream.of(field.getKey()))
                                .toList(), searchField.getKey()
                        ) > 1))
                .distinct().findAny().isEmpty()) {
            searchFieldRepository.saveAll(searchFields);
        }
    }

    public void deleteSearchField(String documentDefinitionName, String key) {
        denyAuthorization();

        searchFieldRepository.findByIdDocumentDefinitionNameAndKey(documentDefinitionName, key).ifPresent(
                searchFieldRepository::delete);
    }

    private SearchField toOrderedSearchField(String documentDefinitionName, SearchFieldDto searchFieldDto, int order) {
        Optional<SearchField> fieldToUpdate = searchFieldRepository
                .findByIdDocumentDefinitionNameAndKey(documentDefinitionName, searchFieldDto.getKey());
        if (fieldToUpdate.isEmpty()) {
            throw new IllegalArgumentException("No search field found for document '" + documentDefinitionName + "' and key '" + searchFieldDto.getKey() + "'.");
        }
        var searchField = fieldToUpdate.get();
        searchField.setPath(searchFieldDto.getPath());
        searchField.setDataType(searchFieldDto.getDataType());
        searchField.setFieldType(searchFieldDto.getFieldType());
        searchField.setMatchType(searchFieldDto.getMatchType());
        searchField.setDropdownDataProvider(searchFieldDto.getDropdownDataProvider());
        searchField.setOrder(order);
        searchField.setTitle(searchFieldDto.getTitle());
        return searchField;
    }


    private void validateSearchField(SearchFieldDto searchFieldDto) {
        if (!searchFieldDto.getDataType().equals(SearchFieldDataType.TEXT)
                && !searchFieldDto.getMatchType().equals(SearchFieldMatchType.EXACT)) {
            throw new InvalidSearchFieldException(
                    "Match type " + searchFieldDto.getMatchType().toString()
                            + " is invalid for data type " + searchFieldDto.getDataType(),
                    Status.BAD_REQUEST
            );
        }
        if (searchFieldDto.getDataType().equals(SearchFieldDataType.BOOLEAN)
                && searchFieldDto.getFieldType().equals(SearchFieldFieldType.RANGE)) {
            throw new InvalidSearchFieldException(
                    "Field type " + searchFieldDto.getFieldType().toString()
                            + " is invalid for data type " + searchFieldDto.getDataType(),
                    Status.BAD_REQUEST
            );
        }
        if ((searchFieldDto.getFieldType().equals(SearchFieldFieldType.MULTI_SELECT_DROPDOWN)
            || searchFieldDto.getFieldType().equals(SearchFieldFieldType.SINGLE_SELECT_DROPDOWN))
            && searchFieldDto.getDropdownDataProvider() == null) {
            throw new InvalidSearchFieldException(
                "Field type " + searchFieldDto.getFieldType().toString()
                    + " must have a datasource for the dropdown list.",
                Status.BAD_REQUEST
            );
        }
    }

    private void denyAuthorization() {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                SearchField.class,
                Action.deny(),
                null
            )
        );
    }
}

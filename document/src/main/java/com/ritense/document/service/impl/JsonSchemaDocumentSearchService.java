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

package com.ritense.document.service.impl;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
public class JsonSchemaDocumentSearchService implements DocumentSearchService {

    private final DocumentRepository<JsonSchemaDocument> documentRepository;
    private final EntityManager entityManager;

    @Override
    public Page<JsonSchemaDocument> search(
        final String documentDefinitionName,
        final List<SearchCriteria> criteriaList,
        final Pageable pageable
    ) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JsonSchemaDocument> query = cb.createQuery(JsonSchemaDocument.class);
        final Root<JsonSchemaDocument> documentRoot = query.from(JsonSchemaDocument.class);
        final List<Predicate> predicateList = criteriaList
            .stream()
            .map(searchCriteria -> isNotNull(cb, documentRoot, searchCriteria.getPath(), searchCriteria.getValue()))
            .collect(Collectors.toList());

        query.where(
            cb.equal(documentRoot.get("documentDefinitionId").get("name"), documentDefinitionName),
            cb.or(predicateList.toArray(Predicate[]::new))
        );

        final TypedQuery<JsonSchemaDocument> typedQuery = entityManager
            .createQuery(query)
            .setFirstResult(pageable.getPageNumber())
            .setMaxResults(pageable.getPageSize());

        return new PageImpl<>(typedQuery.getResultList());
    }

    @Override
    public Page<JsonSchemaDocument> search(
        final String documentDefinitionName,
        final String searchCriteria,
        final Long sequence,
        final String createdBy,
        final Pageable pageable
    ) {
        return documentRepository.searchByCriteria(documentDefinitionName, searchCriteria, sequence, createdBy, pageable);
    }

    private Predicate isNotNull(CriteriaBuilder cb, Root<JsonSchemaDocument> root, String path, String value) {
        return cb.isNotNull(
            cb.function(
                "JSON_SEARCH",
                JsonSchemaDocument.class,
                cb.function("lower", String.class, root.get("content").get("content")),
                cb.literal("all"),
                cb.function("lower", String.class, cb.literal("%" + value.trim() + "%")),
                cb.nullLiteral(String.class),
                cb.function("lower", String.class, cb.literal(path))
            )
        );
    }

}
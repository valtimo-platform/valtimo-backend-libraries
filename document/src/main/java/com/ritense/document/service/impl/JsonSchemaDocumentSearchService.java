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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.service.DocumentSearchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
@Transactional
public class JsonSchemaDocumentSearchService implements DocumentSearchService {

    private static final String LOWER_CASE_FUNTION = "lower";

    private final EntityManager entityManager;

    @Override
    public Page<JsonSchemaDocument> search(
        final SearchRequest searchRequest,
        final Pageable pageable
    ) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JsonSchemaDocument> query = cb.createQuery(JsonSchemaDocument.class);
        final Root<JsonSchemaDocument> documentRoot = query.from(JsonSchemaDocument.class);
        final List<Predicate> predicates = new ArrayList<>();

        addNonJsonFieldPredicates(cb, documentRoot, searchRequest, predicates);
        addJsonFieldPredicates(cb, documentRoot, searchRequest, predicates);

        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(getOrderBy(cb, documentRoot, pageable.getSort()));

        final TypedQuery<JsonSchemaDocument> typedQuery = entityManager
            .createQuery(query)
            .setFirstResult((int)pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(JsonSchemaDocument.class)));
        countQuery.where(predicates.toArray(Predicate[]::new));

        return new PageImpl<>(typedQuery.getResultList(), pageable, entityManager.createQuery(countQuery).getSingleResult());
    }

    private void addNonJsonFieldPredicates(CriteriaBuilder cb, Root<JsonSchemaDocument> root,
        SearchRequest searchRequest, List<Predicate> predicates) {

        if (!StringUtils.isEmpty(searchRequest.getDocumentDefinitionName())) {
            predicates.add(cb.equal(root.get("documentDefinitionId").get("name"),
                searchRequest.getDocumentDefinitionName()));
        }

        if (!StringUtils.isEmpty(searchRequest.getCreatedBy())) {
            predicates.add(cb.equal(root.get("createdBy"), searchRequest.getCreatedBy()));
        }

        if (searchRequest.getSequence() != null) {
            predicates.add(cb.equal(root.get("sequence"), searchRequest.getSequence()));
        }

        if (!StringUtils.isEmpty(searchRequest.getGlobalSearchFilter())) {
            predicates.add(findJsonValue(cb, root, searchRequest.getGlobalSearchFilter()));
        }

    }

    private void addJsonFieldPredicates(CriteriaBuilder cb, Root<JsonSchemaDocument> root,
        SearchRequest searchRequest, List<Predicate> predicates) {

        if (searchRequest.getOtherFilters() != null && !searchRequest.getOtherFilters().isEmpty()) {
            Map<String, List<SearchCriteria>> criteriaPerPath = searchRequest.getOtherFilters()
                .stream()
                .collect(groupingBy(SearchCriteria::getPath));

            List<Predicate> criteriaPredicates = criteriaPerPath.entrySet()
                .stream()
                .flatMap(pathEntry -> {
                    if (pathEntry.getValue().size() == 1) {
                        return Stream.of(
                            findJsonPathValue(cb, root, pathEntry.getKey(), pathEntry.getValue().get(0).getValue()));
                    } else {
                        return Stream.of(cb.or(
                            pathEntry.getValue().stream()
                                .map(currentCriteria -> findJsonPathValue(cb, root, currentCriteria.getPath(),
                                    currentCriteria.getValue()))
                                .collect(toList())
                                .toArray(Predicate[]::new)
                        ));
                    }
                })
                .collect(toList());

            predicates.add(cb.and(criteriaPredicates.toArray(Predicate[]::new)));
        }
    }

    private Predicate findJsonPathValue(CriteriaBuilder cb, Root<JsonSchemaDocument> root, String path, String value) {
        return cb.isNotNull(
            cb.function(
                "JSON_SEARCH",
                JsonSchemaDocument.class,
                cb.function(LOWER_CASE_FUNTION, String.class, root.get("content").get("content")),
                cb.literal("all"),
                cb.function(LOWER_CASE_FUNTION, String.class, cb.literal("%" + value.trim() + "%")),
                cb.nullLiteral(String.class),
                cb.function(LOWER_CASE_FUNTION, String.class, cb.literal(path))
            )
        );
    }

    private Predicate findJsonValue(CriteriaBuilder cb, Root<JsonSchemaDocument> root, String value) {
        return cb.isNotNull(
            cb.function(
                "JSON_SEARCH",
                JsonSchemaDocument.class,
                cb.function(LOWER_CASE_FUNTION, String.class, root.get("content").get("content")),
                cb.literal("all"),
                cb.function(LOWER_CASE_FUNTION, String.class, cb.literal("%" + value.trim() + "%"))
            )
        );
    }

    private List<Order> getOrderBy(CriteriaBuilder cb, Root<JsonSchemaDocument> root, Sort sort) {
        return sort.stream()
            .map(order -> {
                if (order.getProperty().startsWith("$.")) {
                    return new OrderImpl(
                        cb.function(LOWER_CASE_FUNTION, String.class,
                            cb.function(
                                "JSON_EXTRACT",
                                JsonSchemaDocument.class,
                                root.get("content"),
                                cb.literal(order.getProperty()))
                        ),
                        order.getDirection().isAscending());
                } else {
                    return new OrderImpl(
                        root.get(order.getProperty()),
                        order.getDirection().isAscending());
                }
            })
            .collect(Collectors.toList());
    }

}
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

package com.ritense.audit.service.impl;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.service.AuditSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
public class AuditSearchServiceImpl implements AuditSearchService {

    private final EntityManager entityManager;

    @Override
    public Page<AuditRecord> search(List<SearchCriteria> criteriaList, Pageable pageable) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditRecord> query = cb.createQuery(AuditRecord.class);
        Root<AuditRecord> root = query.from(AuditRecord.class);

        final List<Predicate> predicateList = criteriaList
            .stream()
            .map((searchCriteria) ->
                cb.and(
                    isNotNull(cb, root, searchCriteria.getPath(), searchCriteria.getValue()),
                    isNotNull(cb, root, "$.className", searchCriteria.getAuditEvent().getName())
                )
            ).collect(Collectors.toList());

        query
            .where(cb.or(predicateList.toArray(Predicate[]::new)))
            .orderBy(cb.desc(root.get("metaData").get("occurredOn")));

        final TypedQuery<AuditRecord> typedQuery = entityManager
            .createQuery(query)
            .setFirstResult(pageable.getPageNumber())
            .setMaxResults(pageable.getPageSize());

        return new PageImpl<>(typedQuery.getResultList());
    }

    private Predicate getEqualPredicate(CriteriaBuilder builder, Root<AuditRecord> root, String path, String value) {
        return builder.equal(
            builder.function(
                "JSON_EXTRACT",
                AuditRecord.class,
                root.get("auditEvent"),
                builder.literal(path)
            ),
            value
        );
    }

    private Predicate isNotNull(CriteriaBuilder cb, Root<AuditRecord> root, String path, String value) {
        return cb.isNotNull(
            cb.function(
                "JSON_SEARCH",
                AuditRecord.class,
                cb.function("lower", String.class, root.get("auditEvent")),
                cb.literal("all"),
                cb.function("lower", String.class, cb.literal(value)),
                cb.nullLiteral(String.class),
                cb.function("lower", String.class, cb.literal(path))
            )
        );

    }

}
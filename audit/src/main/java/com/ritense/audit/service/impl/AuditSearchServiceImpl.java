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

package com.ritense.audit.service.impl;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.service.AuditSearchService;
import com.ritense.authorization.Action;
import com.ritense.authorization.EntityAuthorizationRequest;
import com.ritense.authorization.AuthorizationService;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AuditSearchServiceImpl implements AuditSearchService {

    private final EntityManager entityManager;

    private final QueryDialectHelper queryDialectHelper;

    private final AuthorizationService authorizationService;

    public AuditSearchServiceImpl(
        EntityManager entityManager,
        QueryDialectHelper queryDialectHelper,
        AuthorizationService authorizationService
    ) {
        this.entityManager = entityManager;
        this.queryDialectHelper = queryDialectHelper;
        this.authorizationService = authorizationService;
    }

    @Override
    public Page<AuditRecord> search(List<SearchCriteria> criteriaList, Pageable pageable) {
        denyAuthorization();
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

    private Predicate isNotNull(CriteriaBuilder cb, Root<AuditRecord> root, String path, String value) {
        return queryDialectHelper.getJsonValueExistsInPathExpression(cb, root.get("auditEvent"), path, value);
    }

    private void denyAuthorization() {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                AuditRecord.class,
                Action.deny()
            ),
            null,
            null
        );
    }

}
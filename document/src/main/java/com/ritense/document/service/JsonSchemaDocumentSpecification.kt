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

import com.ritense.authorization.AuthorizationSpecification;
import com.ritense.authorization.AuthorizationRequest;
import com.ritense.authorization.permission.Permission;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import org.jetbrains.annotations.NotNull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonSchemaDocumentSpecification extends AuthorizationSpecification<JsonSchemaDocument> {
    private List<Permission> permissions;
    private AuthorizationRequest<JsonSchemaDocument> authContext;
    private QueryDialectHelper queryDialectHelper;

    public JsonSchemaDocumentSpecification(
        List<Permission> permissions,
        @NotNull AuthorizationRequest<JsonSchemaDocument> authContext,
        QueryDialectHelper queryDialectHelper
    ) {
        super(permissions, authContext);
        this.permissions = permissions;
        this.authContext = authContext;
        this.queryDialectHelper = queryDialectHelper;
    }

    @NotNull
    @Override
    public Predicate toPredicate(
        Root<JsonSchemaDocument> root,
        CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder
    ) {
        // Filter the permissions for the relevant ones and use those to  find the filters that are required
        // Turn those filters into predicates
        if (query.getGroupList().isEmpty()) {
            ArrayList<Expression<?>> groupList = new ArrayList<>(query.getGroupList());
            groupList.add(root.get("id").get("id"));
            query.groupBy(groupList);
        }

        List<Predicate> predicates = permissions.stream().filter(permission ->
                JsonSchemaDocument.class.equals(permission.getResourceType()) &&
                    authContext.getAction().equals(permission.getAction()))
            .map(permission ->
                permission.toPredicate(root, query, criteriaBuilder, authContext.getResourceType(), queryDialectHelper)
            ).collect(Collectors.toList());

        return combinePredicates(criteriaBuilder, predicates);
    }

}

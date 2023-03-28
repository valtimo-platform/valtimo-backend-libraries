package com.ritense.document.service;

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationRequest;
import com.ritense.authorization.AuthorizationFilter;
import com.ritense.authorization.AuthorizationSpecification;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;

public class JsonSchemaDocumentSpecification implements AuthorizationSpecification<JsonSchemaDocument> {
    private Map<AuthorizationFilter, List<AuthorizationFilter>> filters;
    private QueryDialectHelper queryDialectHelper;

    public JsonSchemaDocumentSpecification(List<AuthorizationFilter> filters, QueryDialectHelper queryDialectHelper) {
        super();
        this.filters = filters;
        this.queryDialectHelper = queryDialectHelper;
    }

    @Override
    public Predicate toPredicate(
        Root<JsonSchemaDocument> root,
        CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> predicates = filters.stream().map(
            filter -> {
                if (filter.getField().startsWith("$.")) {
                    return jsonPathPredicate(root, criteriaBuilder, filter);
                } else {
                    return documentPredicate(root, criteriaBuilder, filter);
                }
            }
        ).toList();

        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private Predicate jsonPathPredicate(Root<JsonSchemaDocument> root,
        CriteriaBuilder criteriaBuilder,
        AuthorizationFilter filter
    ) {
        return queryDialectHelper
            .getJsonValueExistsInPathExpression(
                criteriaBuilder,
                root.get("content").get("content"), filter.getField(), filter.getValue());
    }

    private Predicate documentPredicate(Root<JsonSchemaDocument> root,
        CriteriaBuilder criteriaBuilder,
        AuthorizationFilter filter
    ) {
        if (filter.getField().equals("documentDefinitionId.name")) {
            return criteriaBuilder.equal(root.get("documentDefinitionId").get("name"), filter.getValue());
        }

        return criteriaBuilder.equal(root.get(filter.getField()), filter.getValue());
    }

    @Override
    public boolean isAuthorized(@NotNull AuthorizationRequest<JsonSchemaDocument> authContext) {

        return (authContext.getResources().contains("leningen") && Action.CLAIM != authContext.getAction());
    }
}

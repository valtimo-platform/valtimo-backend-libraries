package com.ritense.document.service;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class JsonSchemaDocumentSpecification implements Specification<JsonSchemaDocument> {
    private List<PBACFilter> filters;
    private QueryDialectHelper queryDialectHelper;

    public JsonSchemaDocumentSpecification(List<PBACFilter> filters, QueryDialectHelper queryDialectHelper) {
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
                if (filter.field.startsWith("$.")) {
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
        PBACFilter filter
    ) {
        return queryDialectHelper
            .getJsonValueExistsInPathExpression(
                criteriaBuilder,
                root.get("content").get("content"), filter.field, filter.value);
    }

    private Predicate documentPredicate(Root<JsonSchemaDocument> root,
        CriteriaBuilder criteriaBuilder,
        PBACFilter filter
    ) {
        return criteriaBuilder.equal(root.get(filter.field), filter.value);
    }
}

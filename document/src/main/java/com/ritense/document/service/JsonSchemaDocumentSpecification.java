package com.ritense.document.service;

import com.ritense.authorization.AuthorizationRequest;
import com.ritense.authorization.AuthorizationSpecification;
import com.ritense.authorization.permission.Permission;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;

public class JsonSchemaDocumentSpecification extends AuthorizationSpecification<JsonSchemaDocument> {
    private List<Permission> permissions;
    private AuthorizationRequest<JsonSchemaDocument> authContext;
    private QueryDialectHelper queryDialectHelper;

    public JsonSchemaDocumentSpecification(
        List<Permission> permissions,
        @NotNull AuthorizationRequest<JsonSchemaDocument> authContext,
        QueryDialectHelper queryDialectHelper) {
        super(permissions, authContext);
        this.permissions = permissions;
        this.authContext = authContext;
        this.queryDialectHelper = queryDialectHelper;
    }

    @Override
    public Predicate toPredicate(
        Root<JsonSchemaDocument> root,
        CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder
    ) {
        // Filter the permissions for the relevant ones and use those to  find the filters that are required
        // Turn those filters into predicates
        List<Predicate> predicates = new java.util.ArrayList<>(List.of());

        permissions.stream().filter(permission ->
                JsonSchemaDocument.class.equals(permission.getResourceType())
                    && authContext.getAction().equals(permission.getAction()))
            .forEach(
                permission -> predicates.add(permission.toPredicate(root, query, criteriaBuilder, authContext.getResourceType(), queryDialectHelper)));
        return combinePredicates(criteriaBuilder, predicates);
    }

}
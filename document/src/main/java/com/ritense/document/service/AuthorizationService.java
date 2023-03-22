package com.ritense.document.service;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class AuthorizationService {
    // Imaginary collection of specifications, could be anything really. Sky's the limit, just needs to be expandable
    private QueryDialectHelper queryDialectHelper;

    public AuthorizationService(QueryDialectHelper queryDialectHelper) {
        this.queryDialectHelper = queryDialectHelper;
    }

    public <T> Specification<T> hasPermission(Class<T> forClass) {
        if (forClass.equals(JsonSchemaDocument.class)) {
            return (Specification<T>)
                new JsonSchemaDocumentSpecification(
                    List.of(new PBACFilter("$.voornaam", "Peter", "HALLO IK BEN EEN PANNENKOEK")), queryDialectHelper);
        } else {
            return null;
        }
    }
}

/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.database;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class MysqlQueryDialectHelper implements QueryDialectHelper {

    private static final String LOWER_CASE_FUNCTION = "lower";

    @Override
    public Expression<String> getJsonValueExpression(CriteriaBuilder cb, Path column, String path) {
        return getJsonValueExpression(cb, column, path, String.class);
    }

    @Override
    public <T> Expression<T> getJsonValueExpression(CriteriaBuilder cb, Path column, String path, Class<T> type) {
        return cb.function(
            "JSON_EXTRACT",
            type,
            column,
            cb.literal(path)
        );
    }

    @Override
    public Predicate getJsonValueExistsExpression(CriteriaBuilder cb, Path column, String value) {
        return cb.isNotNull(
            cb.function(
                "JSON_SEARCH",
                String.class,
                cb.function(LOWER_CASE_FUNCTION, String.class, column),
                cb.literal("all"),
                cb.function(LOWER_CASE_FUNCTION, String.class, cb.literal("%" + value.trim() + "%"))
            )
        );
    }

    @Override
    public Predicate getJsonValueExistsInPathExpression(CriteriaBuilder cb, Path column, String path,
        String value) {
        return cb.isNotNull(
            cb.function(
                "JSON_SEARCH",
                String.class,
                cb.function(LOWER_CASE_FUNCTION, String.class, column),
                cb.literal("all"),
                cb.function(LOWER_CASE_FUNCTION, String.class, cb.literal("%" + value.trim() + "%")),
                cb.nullLiteral(String.class),
                cb.function(LOWER_CASE_FUNCTION, String.class, cb.literal(path))
            )
        );
    }
}

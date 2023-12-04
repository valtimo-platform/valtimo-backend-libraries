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

package com.ritense.valtimo.contract.database;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.temporal.TemporalAccessor;

public class MysqlQueryDialectHelper implements QueryDialectHelper {

    private static final String LOWER_CASE_FUNCTION = "lower";

    @Override
    public <T> Expression<T> getJsonValueExpression(CriteriaBuilder cb, Path column, String path, Class<T> type) {
        var jsonValue = cb.function(
            "JSON_EXTRACT",
            Object.class,
            column,
            cb.literal(path)
        );
        if (CharSequence.class.isAssignableFrom(type) || TemporalAccessor.class.isAssignableFrom(type)) {
            // Strings or timestamps extracted from JSON have additional quotes ("") around them in MySQL 5.7.
            return cb.function("JSON_UNQUOTE", type, jsonValue);
        } else if (Boolean.class.isAssignableFrom(type)) {
            // Booleans extracted from JSON can be true/false while MySQL only accepts 1/0.
            return cb.function("IF", type, jsonValue, cb.literal(1), cb.literal(0));
        } else {
            return jsonValue.as(type);
        }
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

    @Override
    public Predicate getJsonArrayContainsExpression(CriteriaBuilder cb, Path column, String path, String value) {
        return cb.isTrue(cb.function(
            "JSON_CONTAINS",
            Boolean.class,
            column,
            cb.function("JSON_QUOTE", Object.class, cb.literal(value)),
            cb.literal(path)
        ));
    }
}

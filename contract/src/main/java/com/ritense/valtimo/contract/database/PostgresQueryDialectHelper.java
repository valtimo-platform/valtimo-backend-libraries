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

import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class PostgresQueryDialectHelper implements QueryDialectHelper {

    private static final String LOWER_CASE_FUNCTION = "lower";

    @Override
    public <T> Expression<T> getJsonValueExpression(CriteriaBuilder cb, Path column, String jsonPath, Class<T> type) {
        var jsonValue = cb.function(
            "jsonb_path_query_first",
            Object.class,
            column,
            cb.function("jsonpath", String.class, cb.literal(jsonPath))
        );
        if (String.class.isAssignableFrom(type)) {
            return cb.trim('"', jsonValue.as(String.class)).as(type);
        } else if (TemporalAccessor.class.isAssignableFrom(type)) {
            return cb.selectCase()
                .when(jsonValue.as(String.class).in("\"\""), cb.nullLiteral(type))
                .otherwise(cb.trim('"', jsonValue.as(String.class)))
                .as(type);
        } else {
            return jsonValue.as(type);
        }
    }

    @Override
    public Predicate getJsonValueExistsExpression(CriteriaBuilder cb, Path column, String value) {
        return cb.isTrue(
            cb.function(
                "jsonb_path_exists",
                Boolean.class,
                column,
                cb.function(
                    "jsonpath",
                    String.class,
                    cb.literal("$.** ? (@ like_regex \"" + value + "\")")
                )
            )
        );
    }

    @Override
    public Predicate getJsonValueExistsInPathExpression(CriteriaBuilder cb, Path column, String path, String value) {
        return cb.like(
            cb.function(
                LOWER_CASE_FUNCTION,
                String.class,
                getValueForPathText(cb, column, path)
            )
            , "%" + value.toLowerCase() + "%"
        );
    }

    private Expression<String> getValueForPathText(CriteriaBuilder cb, Path column, String path) {
        List<Expression<String>> pathParts = splitPath(path).stream().map(cb::literal).toList();
        Expression[] expressions = new Expression[pathParts.size() + 1];
        expressions[0] = column;
        System.arraycopy(pathParts.toArray(), 0, expressions, 1, pathParts.size());

        return cb.function(
            "jsonb_extract_path_text",
            String.class,
            expressions
        );
    }

    @Override
    public  <T> Expression<T> getValueForPath(CriteriaBuilder cb, Path column, String path, Class<T> type) {
        List<Expression<String>> pathParts = splitPath(path).stream().map(cb::literal).toList();
        Expression[] expressions = new Expression[pathParts.size() + 1];
        expressions[0] = column;
        System.arraycopy(pathParts.toArray(), 0, expressions, 1, pathParts.size());

        return cb.function(
            "jsonb_extract_path",
            type,
            expressions
        );
    }

    private List<String> splitPath(String path) {
        String pathToSplit;
        if (path.startsWith("$.")) {
            pathToSplit = path.substring(2);
        } else {
            pathToSplit = path;
        }
        return Arrays.asList(pathToSplit.split("\\."));
    }
}

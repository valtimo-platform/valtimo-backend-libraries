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
import java.util.UUID;

public interface QueryDialectHelper {

    <T> Expression<T> getJsonValueExpression(CriteriaBuilder cb, Path column, String path, Class<T> type);

    Predicate getJsonValueExistsExpression(CriteriaBuilder cb, Path column, String value);

    Predicate getJsonValueExistsInPathExpression(CriteriaBuilder cb, Path column, String path, String value);

    Predicate getJsonArrayContainsExpression(CriteriaBuilder cb, Path column, String path, String value);

    Expression<String> uuidToString(CriteriaBuilder cb, Path<UUID> column);
}

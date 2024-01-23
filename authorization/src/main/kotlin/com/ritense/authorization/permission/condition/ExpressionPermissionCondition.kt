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

package com.ritense.authorization.permission.condition

import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.annotation.JsonView
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.ritense.authorization.jackson.ComparableDeserializer
import com.ritense.authorization.permission.PermissionView
import com.ritense.authorization.permission.condition.ExpressionPermissionCondition.Companion.EXPRESSION
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.contract.json.MapperSingleton
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root


@JsonTypeName(EXPRESSION)
data class ExpressionPermissionCondition<V : Comparable<V>>(
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val field: String,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val path: String,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val operator: PermissionConditionOperator,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    @JsonDeserialize(using = ComparableDeserializer::class)
    val value: V?,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val clazz: Class<V>
) : ReflectingPermissionCondition(PermissionConditionType.EXPRESSION) {
    override fun <E : Any> isValid(entity: E): Boolean {
        val jsonValue = toJsonString(entity)
            ?: return value == null
        val pathValue = try {
            JsonPath.read<Any?>(jsonValue, path)
        } catch (e: PathNotFoundException) {
            null
        }

        if (pathValue != null && pathValue !is Collection<*> && pathValue.javaClass != clazz) {
            return false
        }

        return evaluateExpression(
            pathValue
        )

    }

    override fun <E : Any> toPredicate(
        root: Root<E>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<E>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path: Path<Any>? = createDatabaseObjectPath(field, root)
        val resolvedValue = PermissionConditionValueResolver.resolveValue(value)

        // we need an exception for json contains
        if (operator == PermissionConditionOperator.LIST_CONTAINS) {
            if (Collection::class.java.isAssignableFrom(clazz)) {
                return queryDialectHelper.getJsonArrayContainsExpression(
                    criteriaBuilder, path, this.path, resolvedValue.toString()
                )
            } else {
                throw IllegalStateException("PBAC: Unsupported 'contains' for clazz '$clazz'")
            }
        }

        return operator.toPredicate<Comparable<Any>>(
            criteriaBuilder,
            queryDialectHelper.getJsonValueExpression(criteriaBuilder, path, this.path, clazz),
            resolvedValue
        )
    }

    private fun toJsonString(entity: Any): String? {
        val fieldValue = findEntityFieldValue(entity, field) ?: return null
        return if (fieldValue is String) {
            fieldValue
        } else {
            try {
                MapperSingleton.get().writeValueAsString(fieldValue) ?: return null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun evaluateExpression(pathValue: Any?): Boolean {
        return operator.evaluate(
            pathValue,
            PermissionConditionValueResolver.resolveValue(value)
        )
    }

    companion object {
        const val EXPRESSION = "expression"
    }
}
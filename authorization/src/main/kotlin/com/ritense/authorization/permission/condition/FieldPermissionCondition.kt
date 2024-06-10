/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
import com.ritense.authorization.permission.PermissionView
import com.ritense.authorization.permission.condition.FieldPermissionCondition.Companion.FIELD
import com.ritense.authorization.request.NestedEntity
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

@JsonTypeName(FIELD)
data class FieldPermissionCondition<V>(
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val field: String,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val operator: PermissionConditionOperator,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val value: V? = null,
) : ReflectingPermissionCondition(PermissionConditionType.FIELD) {
    init {
        require(value == null || value is Comparable<*> || value is List<*>)
    }

    override fun <T : Any> isValid(entity: T): Boolean {
        val fieldValue = findEntityFieldValue(entity, field)
        val resolvedValue = resolveValue()
        return operator.evaluate(fieldValue, resolvedValue)
    }

    override fun <T : Any> isValid(entity: T, nestedEntities: List<NestedEntity<*>>): Boolean {
        return isValid(entity)
    }

    override fun <T : Any> toPredicate(
        root: Root<T>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path = createDatabaseObjectPath(field, root)!!
        val resolvedValue = resolveValue()

        return operator.toPredicate<Comparable<Any>>(criteriaBuilder, path, resolvedValue)
    }

    private fun resolveValue(): Any? {
        return if (this.value is List<*>) {
            this.value.map {
                PermissionConditionValueResolver.resolveValue(it)
            }
        } else {
            PermissionConditionValueResolver.resolveValue(this.value)
        }
    }

    companion object {
        const val FIELD = "field"
    }
}

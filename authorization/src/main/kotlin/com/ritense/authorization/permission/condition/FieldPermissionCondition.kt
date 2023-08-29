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
import com.ritense.authorization.jackson.ComparableDeserializer
import com.ritense.authorization.permission.PermissionView
import com.ritense.authorization.permission.condition.FieldPermissionCondition.Companion.FIELD
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@JsonTypeName(FIELD)
data class FieldPermissionCondition<V : Comparable<V>>(
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val field: String,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val operator: PermissionConditionOperator,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    @JsonDeserialize(using = ComparableDeserializer::class)
    val value: V?
) : ReflectingPermissionCondition(PermissionConditionType.FIELD) {
    override fun <T : Any> isValid(entity: T): Boolean {
        val fieldValue = findEntityFieldValue(entity, field)
        val resolvedValue = PermissionConditionValueResolver.resolveValue(this.value)
        return operator.evaluate(fieldValue, resolvedValue)
    }

    override fun <T : Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path = createDatabaseObjectPath(field, root)!!
        val resolvedValue = PermissionConditionValueResolver.resolveValue(this.value)
        return operator.toPredicate<Comparable<Any>>(criteriaBuilder, path, resolvedValue)
    }

    companion object {
        const val FIELD = "field"
    }
}

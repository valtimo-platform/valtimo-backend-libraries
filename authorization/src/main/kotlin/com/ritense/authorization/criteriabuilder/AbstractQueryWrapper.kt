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

package com.ritense.authorization.criteriabuilder

import javax.persistence.criteria.AbstractQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.persistence.criteria.Selection
import javax.persistence.criteria.Subquery
import javax.persistence.metamodel.EntityType

class AbstractQueryWrapper<T>(
    val query: AbstractQuery<T>
) : AbstractQuery<T> {

    override fun <X : Any?> from(entity: EntityType<X>): Root<X> = from(entity.javaType)

    override fun <X : Any?> from(entityClass: Class<X>): Root<X> =
        roots.find { it.javaType == entityClass } as Root<X>? ?: query.from(entityClass)

    override fun groupBy(vararg grouping: Expression<*>): AbstractQuery<T> = groupBy(grouping.toList())

    override fun groupBy(grouping: List<Expression<*>>): AbstractQuery<T> {
        val existingGroups = groupList.toMutableSet()
        existingGroups.addAll(grouping)
        return query.groupBy(existingGroups.toList())
    }

    override fun <U : Any?> subquery(type: Class<U>?): Subquery<U> = query.subquery(type)

    override fun getRestriction(): Predicate = query.restriction

    override fun where(restriction: Expression<Boolean>): AbstractQuery<T> = query.where(restriction)

    override fun where(vararg restrictions: Predicate): AbstractQuery<T> = query.where(*restrictions)

    override fun having(restriction: Expression<Boolean>): AbstractQuery<T> = query.having(restriction)

    override fun having(vararg restrictions: Predicate): AbstractQuery<T> = query.having(*restrictions)

    override fun distinct(distinct: Boolean): AbstractQuery<T> = query.distinct(distinct)

    override fun getRoots(): Set<Root<*>> = query.roots

    override fun getSelection(): Selection<T> = query.selection

    override fun getGroupList(): List<Expression<*>> = query.groupList

    override fun getGroupRestriction(): Predicate = query.groupRestriction

    override fun isDistinct(): Boolean = query.isDistinct

    override fun getResultType(): Class<T> = query.resultType

}
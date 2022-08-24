/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.plugin.repository

import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Join
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class PluginConfigurationSearchRepository(
    val entityManager: EntityManager
) {
    fun search(pluginConfigurationSearchParameters: PluginConfigurationSearchParameters): List<PluginConfiguration> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(PluginConfiguration::class.java)
        val selectRoot = query.from(PluginConfiguration::class.java)
        query.select(selectRoot)
        query.where(*createWhereClause(pluginConfigurationSearchParameters, criteriaBuilder, selectRoot, query))

        val typedQuery: TypedQuery<PluginConfiguration> = entityManager
            .createQuery(query)

        return typedQuery.resultList
    }

    private fun createWhereClause(
        pluginConfigurationSearchParameters: PluginConfigurationSearchParameters,
        cb: CriteriaBuilder,
        root: Root<PluginConfiguration>,
        query: CriteriaQuery<PluginConfiguration>
    ): Array<Predicate> {
        val predicates: MutableList<Predicate> = mutableListOf()
        if (pluginConfigurationSearchParameters.activityType != null) {
            predicates.add(activityTypePredicate(pluginConfigurationSearchParameters, cb, root, query))
        }

        if (pluginConfigurationSearchParameters.category != null) {
            predicates.add(categoryPredicate(pluginConfigurationSearchParameters, cb, root, query))
        }
        return predicates.toTypedArray()
    }

    private fun activityTypePredicate(
        pluginConfigurationSearchParameters: PluginConfigurationSearchParameters,
        cb: CriteriaBuilder,
        root: Root<PluginConfiguration>,
        query: CriteriaQuery<PluginConfiguration>
    ): Predicate {
        val subQueryDefinitions = query.subquery(PluginDefinition::class.java)
        val definitionsFrom = subQueryDefinitions.from(PluginDefinition::class.java)
        val actionDefinitionJoin: Join<PluginDefinition, PluginActionDefinition> =
            definitionsFrom.join(PLUGIN_ACTION_DEFINITIONS)

        subQueryDefinitions.select(definitionsFrom)
        subQueryDefinitions.where(
            cb.equal(root.get<PluginDefinition>(PLUGIN_DEFINITION), definitionsFrom),
            cb.isMember(
                pluginConfigurationSearchParameters.activityType, actionDefinitionJoin.get(ACTIVITY_TYPES)
            )
        )

        return cb.exists(subQueryDefinitions)
    }

    private fun categoryPredicate(
        pluginConfigurationSearchParameters: PluginConfigurationSearchParameters,
        cb: CriteriaBuilder,
        root: Root<PluginConfiguration>,
        query: CriteriaQuery<PluginConfiguration>
    ) : Predicate {
        val subQueryDefinitions = query.subquery(PluginDefinition::class.java)
        val definitionsFrom = subQueryDefinitions.from(PluginDefinition::class.java)
        val categoryJoin: Join<PluginDefinition, PluginActionDefinition> =
            definitionsFrom.join(PLUGIN_CATEGORIES)

        subQueryDefinitions.select(definitionsFrom)
        subQueryDefinitions.where(
            cb.equal(root.get<PluginDefinition>(PLUGIN_DEFINITION), definitionsFrom),
            cb.equal(categoryJoin.get<String>(CATEGORY_KEY), pluginConfigurationSearchParameters.category)
        )

        return cb.exists(subQueryDefinitions)
    }

    companion object {
        private const val PLUGIN_DEFINITION = "pluginDefinition"
        private const val PLUGIN_CATEGORIES = "categories"
        private const val PLUGIN_ACTION_DEFINITIONS = "actions"
        private const val ACTIVITY_TYPES = "activityTypes"
        private const val CATEGORY_KEY = "key"
    }
}
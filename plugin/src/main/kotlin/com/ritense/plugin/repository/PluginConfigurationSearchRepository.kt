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

package com.ritense.plugin.repository

import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

class PluginConfigurationSearchRepository(
    val entityManager: EntityManager
) {
    fun search(pluginConfigurationSearchParameters: PluginConfigurationSearchParameters): List<PluginConfiguration> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(PluginConfiguration::class.java)
        val selectRoot = query.from(PluginConfiguration::class.java)
        query.select(selectRoot)
        query.where(*createWhereClause(pluginConfigurationSearchParameters, criteriaBuilder, selectRoot, query))
        query.orderBy(
            criteriaBuilder.asc(
                selectRoot.get<PluginDefinition>(PLUGIN_DEFINITION).get<String>(PLUGIN_DEFINITION_TITLE)
            )
        )

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
        if (pluginConfigurationSearchParameters.pluginDefinitionKey != null) {
            predicates.add(pluginDefinitionKeyPredicate(pluginConfigurationSearchParameters, cb, root, query))
        }

        if (pluginConfigurationSearchParameters.pluginConfigurationTitle != null) {
            predicates.add(pluginConfigurationTitlePredicate(pluginConfigurationSearchParameters, cb, root))
        }

        if (pluginConfigurationSearchParameters.activityType != null) {
            predicates.add(activityTypePredicate(pluginConfigurationSearchParameters, cb, root, query))
        }

        if (pluginConfigurationSearchParameters.category != null) {
            predicates.add(categoryPredicate(pluginConfigurationSearchParameters, cb, root, query))
        }
        return predicates.toTypedArray()
    }

    private fun pluginDefinitionKeyPredicate(
        pluginConfigurationSearchParameters: PluginConfigurationSearchParameters,
        cb: CriteriaBuilder,
        root: Root<PluginConfiguration>,
        query: CriteriaQuery<PluginConfiguration>
    ): Predicate {
        val subQueryDefinitions = query.subquery(PluginDefinition::class.java)
        val definitionsFrom = subQueryDefinitions.from(PluginDefinition::class.java)

        subQueryDefinitions.select(definitionsFrom)
        subQueryDefinitions.where(
            cb.equal(root.get<PluginDefinition>(PLUGIN_DEFINITION), definitionsFrom),
            cb.equal(definitionsFrom.get<String>(PLUGIN_DEFINITION_KEY), pluginConfigurationSearchParameters.pluginDefinitionKey)
        )

        return cb.exists(subQueryDefinitions)
    }

    private fun pluginConfigurationTitlePredicate(
        pluginConfigurationSearchParameters: PluginConfigurationSearchParameters,
        cb: CriteriaBuilder,
        root: Root<PluginConfiguration>,
    ): Predicate {
        return cb.equal(root.get<String>(PLUGIN_CONFIGURATION_TITLE), pluginConfigurationSearchParameters.pluginConfigurationTitle)
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
        private const val PLUGIN_DEFINITION_KEY = "key"
        private const val PLUGIN_DEFINITION_TITLE = "title"
        private const val PLUGIN_CONFIGURATION_TITLE = "title"
        private const val PLUGIN_CATEGORIES = "categories"
        private const val PLUGIN_ACTION_DEFINITIONS = "actions"
        private const val ACTIVITY_TYPES = "activityTypes"
        private const val CATEGORY_KEY = "key"
    }
}

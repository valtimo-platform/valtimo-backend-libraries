package com.ritense.plugin.service

import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginCategory
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginDefinition
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class PluginConfigurationSearchService(
    val entityManager: EntityManager
) {
    fun search(pluginConfigurationSearchParameters: PluginConfigurationSearchParameters): List<PluginConfiguration> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(PluginConfiguration::class.java)
        val selectRoot = query.from(PluginConfiguration::class.java)
        query.select(selectRoot).distinct(true)
        query.where(*createWhereClause(pluginConfigurationSearchParameters, criteriaBuilder, selectRoot))

        val typedQuery: TypedQuery<PluginConfiguration> = entityManager
            .createQuery(query)

        return typedQuery.resultList
    }

    private fun createWhereClause(
        pluginConfigurationSearchParameters: PluginConfigurationSearchParameters,
        cb: CriteriaBuilder,
        root: Root<PluginConfiguration>
    ): Array<Predicate> {
        val predicates: MutableList<Predicate> = mutableListOf()
        if (pluginConfigurationSearchParameters.activityType != null) {
            val definitionJoin: Join<PluginConfiguration, PluginDefinition> =
                root.join(PLUGIN_DEFINITION)
            val actionDefinitionJoin: Join<PluginDefinition, PluginActionDefinition> =
                definitionJoin.join(PLUGIN_ACTION_DEFINITIONS)
            predicates.add(cb.isMember(
                pluginConfigurationSearchParameters.activityType, actionDefinitionJoin.get(ACTIVITY_TYPES)
            ))
        }

        if (pluginConfigurationSearchParameters.category != null) {
            val definitionJoin: Join<PluginConfiguration, PluginDefinition> =
                root.join(PLUGIN_DEFINITION, JoinType.LEFT)

            val categoryJoin: Join<PluginDefinition, PluginCategory> =
                definitionJoin.join(PLUGIN_CATEGORIES, JoinType.LEFT)

            predicates.add(cb.equal(categoryJoin.get<String>(CATEGORY_KEY), pluginConfigurationSearchParameters.category))
        }
        return predicates.toTypedArray()
    }

    companion object {
        private const val PLUGIN_DEFINITION = "pluginDefinition"
        private const val PLUGIN_CATEGORIES = "categories"
        private const val PLUGIN_ACTION_DEFINITIONS = "actions"
        private const val ACTIVITY_TYPES = "activityTypes"
        private const val CATEGORY_KEY = "key"
    }
}
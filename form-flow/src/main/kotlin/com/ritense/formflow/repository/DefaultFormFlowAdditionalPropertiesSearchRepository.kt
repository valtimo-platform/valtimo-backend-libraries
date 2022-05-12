package com.ritense.formflow.repository

import com.ritense.formflow.domain.instance.FormFlowInstance
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class DefaultFormFlowAdditionalPropertiesSearchRepository(
    private val entityManager: EntityManager
): FormFlowAdditionalPropertiesSearchRepository {

    override fun findInstances(additionalProperties: Map<String, Any>): List<FormFlowInstance> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(FormFlowInstance::class.java)
        val root = query.from(FormFlowInstance::class.java)
        query.where(*createPredicates(additionalProperties, criteriaBuilder, root).toTypedArray())

        return entityManager.createQuery(query).resultList
    }

    private fun createPredicates(
        additionalProperties: Map<String, Any>,
        criteriaBuilder: CriteriaBuilder,
        root: Root<FormFlowInstance>
    ): List<Predicate> {
        return additionalProperties.entries.stream().map{
            findJsonPathValue(criteriaBuilder, root, it.key, it.value.toString())
        }.toList()
    }

    private fun findJsonPathValue(
        cb: CriteriaBuilder,
        root: Root<FormFlowInstance>,
        key: String,
        value: String
    ): Predicate {
        return cb.isNotNull(
            cb.function<FormFlowInstance>(
                "JSON_SEARCH",
                FormFlowInstance::class.java,
                root.get<Any>("additionalProperties"),
                cb.literal("all"),
                cb.literal(value),
                cb.nullLiteral(String::class.java),
                cb.literal("$."+ key)
            )
        )
    }

}
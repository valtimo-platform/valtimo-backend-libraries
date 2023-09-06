package com.ritense.formflow.repository

import com.ritense.formflow.domain.instance.FormFlowInstance
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class MySqlFormFlowAdditionalPropertiesSearchRepository(
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
        val predicates: MutableList<Predicate> = mutableListOf()
        additionalProperties.entries.forEach{
            predicates.add(findJsonPathValue(criteriaBuilder, root, it.key, it.value))
        }

        return predicates
    }

    private fun findJsonPathValue(
        criteriaBuilder: CriteriaBuilder,
        root: Root<FormFlowInstance>,
        key: String,
        value: Any
    ): Predicate {
        return criteriaBuilder.equal(
            criteriaBuilder.function(
                "JSON_EXTRACT",
                FormFlowInstance::class.java,
                root.get<Any>("additionalProperties"),
                criteriaBuilder.literal("$.$key")
            ),
            value
        )
    }

}
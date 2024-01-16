package com.ritense.formflow.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.domain.instance.FormFlowInstance
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

class PostgresFormFlowAdditionalPropertiesSearchRepository(
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper
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
                "json_extract_path_text",
                String::class.java,
                root.get<Any>("additionalProperties"),
                criteriaBuilder.literal(key)
            ),
            if (value is String) value else objectMapper.writeValueAsString(value)
        )
    }

}
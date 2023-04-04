package com.ritense.authorization

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

interface AuthorizationEntityMapper<FROM, TO> {
    fun mapTo(entity: FROM): List<TO>

    fun mapQueryTo(root: Root<FROM>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): AuthorizationEntityMapperResult<TO>

    fun appliesTo(fromClass: Class<*>, toClass: Class<*>): Boolean
}
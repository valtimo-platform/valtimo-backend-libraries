package com.ritense.authorization

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

interface AuthorizationEntityMapper<FROM, TO> {
    fun mapRelated(entity: FROM): List<TO>

    fun mapQuery(root: Root<FROM>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): AuthorizationEntityMapperResult<TO>

    fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean
}
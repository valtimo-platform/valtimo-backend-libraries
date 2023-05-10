package com.ritense.authorization.permission

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.permission.ContainerPermissionCondition.Companion.CONTAINER
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@JsonTypeName(CONTAINER)
data class ContainerPermissionCondition<TO : Any>(
    val resourceType: Class<TO>,
    val conditions: List<PermissionCondition>
) : PermissionCondition(PermissionConditionType.CONTAINER) {
    override fun <FROM: Any> isValid(entity: FROM): Boolean {
        val mapper = findMapper(entity::class.java) as AuthorizationEntityMapper<FROM, TO>
        val relatedEntities = mapper.mapRelated(entity)
        val spec = findChildSpecification()
        return relatedEntities.any {relatedEntity ->
            spec.isAuthorized(relatedEntity)
        }
    }

    override fun <T : Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val authorizationEntityMapperResult = findMapper(resourceType).mapQuery(root, query, criteriaBuilder)
        val spec = findChildSpecification()

        return criteriaBuilder.and(
            authorizationEntityMapperResult.joinPredicate,
            spec.toPredicate(
                authorizationEntityMapperResult.root,
                authorizationEntityMapperResult.query,
                criteriaBuilder
            )
        )
    }

    private fun findChildSpecification(): AuthorizationSpecification<TO> {
        return AuthorizationServiceHolder.currentInstance.getAuthorizationSpecification(
            AuthorizationRequest(this.resourceType, null, Action.IGNORE),
            listOf(
                Permission(
                    resourceType = resourceType,
                    action = Action.IGNORE,
                    conditionContainer = ConditionContainer(conditions),
                    roleKey = ""
                )
            )
        )
    }

    private fun <FROM: Any> findMapper(fromType: Class<FROM>): AuthorizationEntityMapper<FROM, TO> {
        return AuthorizationServiceHolder.currentInstance.getMapper(fromType, this.resourceType)
    }

    companion object {
        const val CONTAINER = "container"
    }
}
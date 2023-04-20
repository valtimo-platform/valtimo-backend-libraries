package com.ritense.authorization.permission

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationSpringContextHelper
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class ContainerPermissionCondition<TO : Any>(
    val resourceType: Class<TO>,
    val conditions: List<PermissionCondition>
) : PermissionCondition() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.CONTAINER
    override fun <FROM: Any> isValid(entity: FROM): Boolean {
        val mapper = findMapper(entity::class.java) as AuthorizationEntityMapper<FROM, TO>
        val relatedEntities = mapper.mapTo(entity)
        val spec = findChildSpecification()
        return relatedEntities.any {
            spec.isAuthorized(it)
        }
    }

    override fun <T : Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val authorizationEntityMapperResult = findMapper(resourceType).mapQueryTo(root, query, criteriaBuilder)
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
        return AuthorizationSpringContextHelper.getService().getAuthorizationSpecification(
            AuthorizationRequest(this.resourceType, null, Action.IGNORE),
            listOf(Permission(resourceType, Action.IGNORE, conditions))
        )
    }

    private fun <FROM: Any> findMapper(fromType: Class<FROM>): AuthorizationEntityMapper<FROM, TO> {
        return AuthorizationSpringContextHelper.getService().getMapper(fromType, this.resourceType)
    }
}
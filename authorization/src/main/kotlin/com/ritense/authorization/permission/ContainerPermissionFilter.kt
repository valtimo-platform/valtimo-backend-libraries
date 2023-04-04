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

class ContainerPermissionFilter<TO : Any>(
    val resourceType: Class<TO>,
    val filters: List<PermissionFilter>
) : PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.CONTAINER
    override fun <FROM: Any> isValid(entity: FROM): Boolean {
        // TODO: retrieve list of entities matching the resourceType, that are related to the OG entity, and for each:
        val mapper = findMapper(entity::class.java) as AuthorizationEntityMapper<FROM, TO>
        val relatedEntities = mapper.mapTo(entity)
        val spec = findChildSpecification() as AuthorizationSpecification<TO>
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
        // TODO: derive the correct Root, CriteriaQuery and CriteriaBuilder (do a join or something with another entity or multiple)
        val authorizationEntityMapperResult = findMapper(resourceType).mapQueryTo(root, query, criteriaBuilder)
        val spec = findChildSpecification() as AuthorizationSpecification<TO>

        return criteriaBuilder.and(
            authorizationEntityMapperResult.joinPredicate,
            spec.toPredicate(
                authorizationEntityMapperResult.root,
                authorizationEntityMapperResult.query,
                criteriaBuilder
            )
        )
    }

    private fun findChildSpecification(): AuthorizationSpecification<*> {
        return AuthorizationSpringContextHelper.getService().getAuthorizationSpecification(
            AuthorizationRequest(this.resourceType, null, Action.IGNORE),
            listOf(Permission(resourceType, Action.IGNORE, filters))
        )
    }

    private fun <FROM: Any> findMapper(fromType: Class<FROM>): AuthorizationEntityMapper<FROM, TO> {
        return AuthorizationSpringContextHelper.getService().getMapper(fromType, this.resourceType)
    }
}
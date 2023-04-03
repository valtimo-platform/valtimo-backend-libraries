package com.ritense.authorization.permission

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationSpringContextHelper
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class ContainerPermissionFilter(
    val resourceType: Class<*>,
    val filters: List<PermissionFilter>
) : PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.CONTAINER
    override fun isValid(entity: Any): Boolean {
        // TODO: retrieve list of entities matching the resourceType, that are related to the OG entity, and for each:
        return Permission(resourceType, Action.IGNORE, filters).appliesTo(resourceType, null)
    }

    override fun <T> toPredicate(root: Root<T>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder, resourceType: Class<T>, queryDialectHelper: QueryDialectHelper): Predicate {
        // TODO: derive the correct Root, CriteriaQuery and CriteriaBuilder (do a join or something with another entity or multiple)
        return AuthorizationSpringContextHelper.getService().getAuthorizationSpecification(
            AuthorizationRequest(this.resourceType, null, Action.IGNORE),
            listOf(Permission(resourceType, Action.IGNORE, filters))
        ).toPredicate()
    }
}
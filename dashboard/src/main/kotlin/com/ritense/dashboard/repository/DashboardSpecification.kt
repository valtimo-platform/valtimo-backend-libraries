package com.ritense.dashboard.repository

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.dashboard.domain.Dashboard
import com.ritense.dashboard.service.DashboardService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.*

class DashboardSpecification(
    authRequest: AuthorizationRequest<Dashboard>,
    permissions: List<Permission>,
    private val dashboardService: DashboardService,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<Dashboard>(authRequest, permissions) {
    override fun identifierToEntity(identifier: String): Dashboard {
        return runWithoutAuthorization {
            dashboardService.getDashboard(identifier)
        }
    }

    override fun toPredicate(
        root: Root<Dashboard>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<String>("key"))
        query.groupBy(groupList)

        val predicates = permissions.stream()
            .filter { permission: Permission ->
                Dashboard::class.java == permission.resourceType
                    && authRequest.action == permission.action
            }
            .map { permission: Permission ->
                permission.toPredicate(
                    root,
                    query,
                    criteriaBuilder,
                    authRequest.resourceType,
                    queryDialectHelper
                )
            }.toList()
        return combinePredicates(criteriaBuilder, predicates)
    }
}
package com.ritense.dashboard.repository

import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.authorization.specification.AuthorizationSpecificationFactory
import com.ritense.dashboard.domain.Dashboard
import com.ritense.dashboard.service.DashboardService
import com.ritense.valtimo.contract.database.QueryDialectHelper

class DashboardSpecificationFactory(
    private var dashboardService: DashboardService,
    private var queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecificationFactory<Dashboard> {
    override fun create(
        request: AuthorizationRequest<Dashboard>,
        permissions: List<Permission>
    ): AuthorizationSpecification<Dashboard> {
        return DashboardSpecification(
            request,
            permissions,
            dashboardService,
            queryDialectHelper
        )
    }

    override fun canCreate(request: AuthorizationRequest<*>, permissions: List<Permission>): Boolean {
        return Dashboard::class.java == request.resourceType
    }
}
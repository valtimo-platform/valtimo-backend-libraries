/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.authorization.autoconfigure

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.AuthorizationSupportedHelper
import com.ritense.authorization.ResourceActionProvider
import com.ritense.authorization.UserManagementServiceHolder
import com.ritense.authorization.ValtimoAuthorizationService
import com.ritense.authorization.annotation.RunWithoutAuthorizationAspect
import com.ritense.authorization.deployment.PermissionDeployer
import com.ritense.authorization.deployment.RoleDeployer
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.RoleRepository
import com.ritense.authorization.specification.AuthorizationSpecificationFactory
import com.ritense.authorization.specification.impl.DenyAuthorizationSpecificationFactory
import com.ritense.authorization.specification.impl.NoopAuthorizationSpecificationFactory
import com.ritense.authorization.web.PermissionManagementResource
import com.ritense.authorization.web.PermissionResource
import com.ritense.authorization.web.RoleManagementResource
import com.ritense.authorization.web.security.ValtimoAuthorizationHttpSecurityConfigurer
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["com.ritense.authorization"])
@EntityScan("com.ritense.authorization")
class AuthorizationAutoConfiguration(
    userManagementService: UserManagementService
) {

    init {
        UserManagementServiceHolder(userManagementService)
    }

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(ValtimoAuthorizationHttpSecurityConfigurer::class)
    fun valtimoAuthorizationHttpSecurityConfigurer(): ValtimoAuthorizationHttpSecurityConfigurer {
        return ValtimoAuthorizationHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(AuthorizationService::class)
    fun valtimoAuthorizationService(
        authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
        mappers: List<AuthorizationEntityMapper<*, *>>,
        actionProviders: List<ResourceActionProvider<*>>,
        permissionRepository: PermissionRepository,
        roleRepository: RoleRepository,
        userManagementService: UserManagementService
    ): AuthorizationService {
        val authorizationService = ValtimoAuthorizationService(
            authorizationSpecificationFactories,
            mappers,
            actionProviders,
            permissionRepository,
            userManagementService
        )
        AuthorizationServiceHolder(authorizationService)
        return authorizationService
    }

    @Order(HIGHEST_PRECEDENCE + 1)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["authorizationLiquibaseMasterChangeLogLocation"])
    fun authorizationLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/authorization-master.xml")
    }


    @Bean
    fun permissionConditionTypeModule(): Module {
        return PermissionConditionTypeModule()
    }

    @Bean
    @Order(HIGHEST_PRECEDENCE)
    fun <T : Any> noopAuthorizationSpecificationFactory(): AuthorizationSpecificationFactory<T> {
        return NoopAuthorizationSpecificationFactory()
    }

    @Bean
    @Order(HIGHEST_PRECEDENCE + 1)
    fun <T : Any> denyAuthorizationSpecificationFactory(): AuthorizationSpecificationFactory<T> {
        return DenyAuthorizationSpecificationFactory()
    }

    @Bean
    @ConditionalOnMissingBean(RoleDeployer::class)
    @Order(1)
    fun roleDeployer(
        objectMapper: ObjectMapper,
        roleRepository: RoleRepository,
        changelogService: ChangelogService,
        @Value("\${valtimo.changelog.pbac.clear-tables:false}") clearTables: Boolean
    ): RoleDeployer {
        return RoleDeployer(objectMapper, roleRepository, changelogService, clearTables)
    }

    @Bean
    @ConditionalOnMissingBean(PermissionDeployer::class)
    @Order(2)
    fun permissionDeployer(
        objectMapper: ObjectMapper,
        permissionRepository: PermissionRepository,
        roleRepository: RoleRepository,
        changelogService: ChangelogService,
        @Value("\${valtimo.changelog.pbac.clear-tables:false}") clearTables: Boolean
    ): PermissionDeployer {
        return PermissionDeployer(objectMapper, permissionRepository, roleRepository, changelogService, clearTables)
    }

    @Bean
    @ConditionalOnMissingBean(RoleManagementResource::class)
    fun roleManagementResource(
        roleRepository: RoleRepository,
        permissionRepository: PermissionRepository
    ): RoleManagementResource {
        return RoleManagementResource(roleRepository, permissionRepository)
    }

    @Bean
    @ConditionalOnMissingBean(PermissionResource::class)
    fun permissionResource(
        authorizationService: AuthorizationService
    ): PermissionResource {
        return PermissionResource(authorizationService)
    }

    @Bean
    fun authorizationSupportedHelper(): AuthorizationSupportedHelper {
        return AuthorizationSupportedHelper
    }

    @Bean
    @ConditionalOnMissingBean(PermissionManagementResource::class)
    fun permissionManagementResource(
        permissionRepository: PermissionRepository
    ): PermissionManagementResource {
        return PermissionManagementResource(permissionRepository)
    }

    @Bean
    @ConditionalOnMissingBean(RunWithoutAuthorizationAspect::class)
    fun runWithoutAuthorizationAspect(): RunWithoutAuthorizationAspect {
        return RunWithoutAuthorizationAspect()
    }

}
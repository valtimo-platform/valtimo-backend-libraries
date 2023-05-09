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
import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.authorization.permission.Permission
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.authorization"])
@EntityScan("com.ritense.authorization")
class AuthorizationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthorizationService::class)
    fun valtimoAuthorizationService(
        authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
        mappers: List<AuthorizationEntityMapper<*, *>>,
        defaultPermissions: List<Permission>
    ): AuthorizationService {
        return AuthorizationService(authorizationSpecificationFactories, mappers, defaultPermissions)
    }

    @Bean
    fun authorizationServiceHolder(authorizationService: AuthorizationService): AuthorizationServiceHolder {
        return AuthorizationServiceHolder(authorizationService)
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
    @ConditionalOnMissingBean(name = ["defaultPermissions"])
    fun defaultPermissions(): List<Permission> {
        val jsonSchemaDocumentClassName = "com.ritense.document.domain.impl.JsonSchemaDocument"
        val testRoleKey = "test-role"

        val documentPermissions: List<Permission> = try {
            listOf(
                Permission(
                    resourceType = Class.forName(jsonSchemaDocumentClassName),
                    action = Action.LIST_VIEW,
                    conditions = listOf(),
                    roleKey = testRoleKey
                ),
                Permission(
                    resourceType = Class.forName(jsonSchemaDocumentClassName),
                    action = Action.VIEW,
                    conditions = emptyList(),
                    roleKey = testRoleKey
                ),
                Permission(
                    resourceType = Class.forName(jsonSchemaDocumentClassName),
                    action = Action.CLAIM,
//                    conditions = listOf(
//                        FieldPermissionCondition("documentDefinitionId.name", "leningen"),
//                        ExpressionPermissionCondition(
//                            "content.content",
//                            "$.height",
//                            PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java)
//                    ),
                    roleKey = testRoleKey
                )
            )
        } catch (e: ClassNotFoundException) {
            listOf()
        }

        val notePermissions: List<Permission> = try {
            listOf(
                Permission(
                    resourceType = Class.forName("com.ritense.note.domain.Note"),
                    action = Action.VIEW,
//                    conditions = listOf(
//                        ContainerPermissionCondition(
//                            Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
//                            listOf(
//                                FieldPermissionCondition("documentDefinitionId.name", "leningen"),
//                                ExpressionPermissionCondition(
//                                    "content.content",
//                                    "$.height",
//                                    PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java),
//                                FieldPermissionCondition("assigneeFullName", "Asha Miller")
//                            )
//                        )
//                    )
                    roleKey = testRoleKey
                )
            )
        } catch (e: ClassNotFoundException) {
            listOf()
        }

        return documentPermissions + notePermissions
    }
}
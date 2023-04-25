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

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.permission.ContainerPermissionCondition
import com.ritense.authorization.permission.ExpressionPermissionCondition
import com.ritense.authorization.permission.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionExpressionOperator
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
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

    @Bean
    @ConditionalOnMissingBean(name = ["defaultPermissions"])
    fun defaultPermissions(): List<Permission> {
        val documentPermissions:List<Permission> = try {
            listOf(
                Permission(
                    Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                    Action.LIST_VIEW,
                    listOf()
                ),
                Permission(
                    Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                    Action.VIEW,
                    emptyList()
                ),
                Permission(
                    Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                    Action.CLAIM,
                    listOf(
                        FieldPermissionCondition("documentDefinitionId.name", "leningen"),
                        ExpressionPermissionCondition(
                            "content.content",
                            "$.height",
                            PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java)
                    )
                )
            )
        } catch (e:ClassNotFoundException) {
            listOf()
        }

        val notePermissions:List<Permission> = try {
            listOf(
                Permission(
                    Class.forName("com.ritense.note.domain.Note"),
                    Action.VIEW,
                    listOf(
                        ContainerPermissionCondition(
                            Class.forName("com.ritense.document.domain.impl.JsonSchemaDocument"),
                            listOf(
                                FieldPermissionCondition("documentDefinitionId.name", "leningen"),
                                ExpressionPermissionCondition(
                                    "content.content",
                                    "$.height",
                                    PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java),
                                FieldPermissionCondition("assigneeFullName", "Asha Miller")
                            )
                        )
                    )
                )
            )
        } catch (e:ClassNotFoundException) {
            listOf()
        }

        return documentPermissions + notePermissions
    }
}
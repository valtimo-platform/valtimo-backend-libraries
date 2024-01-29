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

package com.ritense.script.autoconfigure

import com.ritense.script.repository.ScriptRepository
import com.ritense.script.security.config.ScriptHttpSecurityConfigurer
import com.ritense.script.service.ScriptDeploymentService
import com.ritense.script.service.ScriptService
import com.ritense.script.service.ValtimoScriptPrefiller
import com.ritense.script.web.rest.ScriptManagementResource
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["com.ritense.script.repository"])
@EntityScan("com.ritense.script.domain")
class ScriptAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ScriptService::class)
    fun scriptService(
        scriptRepository: ScriptRepository,
    ): ScriptService {
        return ScriptService(
            scriptRepository,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ScriptDeploymentService::class)
    fun scriptDeploymentService(
        resourceLoader: ResourceLoader,
        scriptService: ScriptService,
    ): ScriptDeploymentService {
        return ScriptDeploymentService(
            resourceLoader,
            scriptService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoScriptPrefiller::class)
    fun valtimoScriptPrefiller(
        scriptService: ScriptService,
    ): ValtimoScriptPrefiller {
        return ValtimoScriptPrefiller(
            scriptService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ScriptManagementResource::class)
    fun scriptManagementResource(
        scriptService: ScriptService,
    ): ScriptManagementResource {
        return ScriptManagementResource(
            scriptService,
        )
    }

    @Order(301)
    @Bean
    @ConditionalOnMissingBean(ScriptHttpSecurityConfigurer::class)
    fun scriptHttpSecurityConfigurer(): ScriptHttpSecurityConfigurer {
        return ScriptHttpSecurityConfigurer()
    }

    @Order(HIGHEST_PRECEDENCE + 32)
    @Bean
    @ConditionalOnMissingBean(name = ["scriptLiquibaseMasterChangeLogLocation"])
    fun scriptLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/script-master.xml")
    }
}

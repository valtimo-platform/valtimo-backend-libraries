/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.case.configuration

import com.ritense.case.repository.CaseDefinitionRepository
import com.ritense.case.security.config.CaseHttpSecurityConfigurer
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.CaseDefinitionResource
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        CaseDefinitionRepository::class
    ]
)
@EntityScan(basePackages = ["com.ritense.case.domain"])
class CaseAutoConfiguration {

    @Bean
    fun caseDefinitionResource(
        service: CaseDefinitionService
    ): CaseDefinitionResource {
        return CaseDefinitionResource(service)
    }

    @Bean
    fun caseService(
        repository: CaseDefinitionRepository
    ): CaseDefinitionService {
        return CaseDefinitionService(repository)
    }

    @Order(300)
    @Bean
    @ConditionalOnMissingBean(CaseHttpSecurityConfigurer::class)
    fun caseHttpSecurityConfigurer(): CaseHttpSecurityConfigurer {
        return CaseHttpSecurityConfigurer()
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    @ConditionalOnMissingBean(name = ["caseLiquibaseMasterChangeLogLocation"])
    @Bean
    fun caseLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/case-master.xml")
    }
}
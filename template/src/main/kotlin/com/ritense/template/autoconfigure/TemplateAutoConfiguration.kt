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

package com.ritense.template.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.template.repository.TemplateRepository
import com.ritense.template.security.config.TemplateHttpSecurityConfigurer
import com.ritense.template.service.TemplateDelegationService
import com.ritense.template.service.TemplateDeploymentService
import com.ritense.template.service.TemplateService
import com.ritense.template.service.TemplateValueResolverFactory
import com.ritense.template.web.rest.TemplateManagementResource
import com.ritense.valtimo.contract.annotation.ProcessBean
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
@EnableJpaRepositories(basePackageClasses = [TemplateRepository::class])
@EntityScan("com.ritense.template.domain")
class TemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TemplateService::class)
    fun templateService(
        templateRepository: TemplateRepository,
        objectMapper: ObjectMapper,
        ): TemplateService {
        return TemplateService(
            templateRepository,
            objectMapper,
        )
    }

    @ProcessBean
    @Bean
    @ConditionalOnMissingBean(TemplateDelegationService::class)
    fun templateDelegationService(
        templateService: TemplateService,
        storageService: TemporaryResourceStorageService,
        processDocumentService: ProcessDocumentService,
    ): TemplateDelegationService {
        return TemplateDelegationService(
            templateService,
            storageService,
            processDocumentService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(TemplateDeploymentService::class)
    fun templateDeploymentService(
        resourceLoader: ResourceLoader,
        templateService: TemplateService,
    ): TemplateDeploymentService {
        return TemplateDeploymentService(
            resourceLoader,
            templateService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(TemplateValueResolverFactory::class)
    fun templateValueResolverFactory(
        templateService: TemplateService,
        documentService: DocumentService,
        processDocumentService: ProcessDocumentService,
    ): TemplateValueResolverFactory {
        return TemplateValueResolverFactory(
            templateService,
            documentService,
            processDocumentService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(TemplateManagementResource::class)
    fun templateManagementResource(
        templateService: TemplateService,
    ): TemplateManagementResource {
        return TemplateManagementResource(
            templateService,
        )
    }

    @Order(301)
    @Bean
    @ConditionalOnMissingBean(TemplateHttpSecurityConfigurer::class)
    fun templateHttpSecurityConfigurer(): TemplateHttpSecurityConfigurer {
        return TemplateHttpSecurityConfigurer()
    }

    @Order(HIGHEST_PRECEDENCE + 32)
    @Bean
    @ConditionalOnMissingBean(name = ["templateLiquibaseMasterChangeLogLocation"])
    fun templateLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/template-master.xml")
    }
}

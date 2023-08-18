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

package com.ritense.objectmanagement.autoconfigure

import com.ritense.objectmanagement.autodeployment.ObjectManagementDefinitionDeploymentService
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objectmanagement.security.config.ObjectManagementHttpSecurityConfigurer
import com.ritense.objectmanagement.service.ObjectManagementFacade
import com.ritense.objectmanagement.service.ObjectManagementInfoProviderImpl
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.objectmanagement.web.rest.ObjectManagementResource
import com.ritense.plugin.service.PluginService
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.objectmanagement.repository"])
@EntityScan("com.ritense.objectmanagement.domain")
class ObjectManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectManagementFacade::class)
    fun objectManagementFacade(
        objectManagementRepository: ObjectManagementRepository,
        pluginService: PluginService
    ): ObjectManagementFacade {
        return ObjectManagementFacade(
            objectManagementRepository,
            pluginService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ObjectManagementService::class)
    fun objectManagementService(
        objectManagementRepository: ObjectManagementRepository,
        pluginService: PluginService,
        searchFieldV2Service: SearchFieldV2Service,
        searchListColumnService: SearchListColumnService,
        objectManagementFacade: ObjectManagementFacade
    ): ObjectManagementService {
        return ObjectManagementService(
            objectManagementRepository,
            pluginService,
            searchFieldV2Service,
            searchListColumnService,
            objectManagementFacade
        )
    }

    @Bean
    @ConditionalOnMissingBean(ObjectManagementInfoProviderImpl::class)
    fun objectManagementInfoProvider(objectManagementService: ObjectManagementService): ObjectManagementInfoProviderImpl {
        return ObjectManagementInfoProviderImpl(
            objectManagementService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ObjectManagementResource::class)
    fun objectManagementResource(
        objectManagementService: ObjectManagementService
    ): ObjectManagementResource {
        return ObjectManagementResource(
            objectManagementService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ObjectManagementDefinitionDeploymentService::class)
    fun objectManagementDefinitionDeploymentService(
        resourceLoader: ResourceLoader,
        objectManagementService: ObjectManagementService,
        objectManagementRepository: ObjectManagementRepository,
        applicationEventPublisher: ApplicationEventPublisher
    ): ObjectManagementDefinitionDeploymentService {
        return ObjectManagementDefinitionDeploymentService(
            resourceLoader,
            objectManagementService,
            objectManagementRepository,
            applicationEventPublisher
        )
    }

    @Order(301)
    @Bean
    @ConditionalOnMissingBean(ObjectManagementHttpSecurityConfigurer::class)
    fun objectManagementHttpSecurityConfigurer(): ObjectManagementHttpSecurityConfigurer {
        return ObjectManagementHttpSecurityConfigurer()
    }

}

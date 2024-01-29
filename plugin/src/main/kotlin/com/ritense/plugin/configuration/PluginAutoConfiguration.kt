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

package com.ritense.plugin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.PluginCategoryResolver
import com.ritense.plugin.PluginDefinitionResolver
import com.ritense.plugin.PluginDeploymentListener
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.autodeployment.PluginAutoDeploymentEventListener
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginActionPropertyDefinitionRepository
import com.ritense.plugin.repository.PluginCategoryRepository
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginConfigurationSearchRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.repository.PluginProcessLinkRepositoryImpl
import com.ritense.plugin.repository.PluginPropertyRepository
import com.ritense.plugin.security.config.PluginHttpSecurityConfigurer
import com.ritense.plugin.service.EncryptionService
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.PluginConfigurationResource
import com.ritense.plugin.web.rest.PluginDefinitionResource
import com.ritense.plugin.web.rest.converter.StringToActivityTypeConverter
import com.ritense.valueresolver.ValueResolverService
import jakarta.persistence.EntityManager
import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        PluginActionDefinitionRepository::class,
        PluginActionPropertyDefinitionRepository::class,
        PluginCategoryRepository::class,
        PluginConfigurationRepository::class,
        PluginDefinitionRepository::class,
        PluginProcessLinkRepositoryImpl::class,
        PluginPropertyRepository::class,
    ]
)
@EntityScan(basePackages = ["com.ritense.plugin.domain"])
class PluginAutoConfiguration {

    @Bean
    fun pluginDeploymentListener(
        pluginDefinitionResolver: PluginDefinitionResolver,
        pluginCategoryResolver: PluginCategoryResolver,
        pluginDefinitionRepository: PluginDefinitionRepository,
        pluginCategoryRepository: PluginCategoryRepository,
        pluginActionDefinitionRepository: PluginActionDefinitionRepository,
        pluginActionPropertyDefinitionRepository: PluginActionPropertyDefinitionRepository
    ): PluginDeploymentListener {
        return PluginDeploymentListener(
            pluginDefinitionResolver,
            pluginCategoryResolver,
            pluginDefinitionRepository,
            pluginCategoryRepository,
            pluginActionDefinitionRepository,
            pluginActionPropertyDefinitionRepository
        )
    }

    @Bean
    @ConditionalOnMissingBean(StringToActivityTypeConverter::class)
    fun stringToActivityTypeConverter(): StringToActivityTypeConverter {
        return StringToActivityTypeConverter()
    }

    @Bean
    fun pluginDefinitionResolver(context: ApplicationContext): PluginDefinitionResolver {
        return PluginDefinitionResolver(context)
    }

    @Bean
    fun pluginCategoryResolver(context: ApplicationContext): PluginCategoryResolver {
        return PluginCategoryResolver(context)
    }

    @Order(420)
    @Bean
    @ConditionalOnMissingBean(PluginHttpSecurityConfigurer::class)
    fun pluginHttpSecurityConfigurer(): PluginHttpSecurityConfigurer {
        return PluginHttpSecurityConfigurer()
    }

    @Bean
    fun pluginService(
        pluginDefinitionRepository: PluginDefinitionRepository,
        pluginConfigurationRepository: PluginConfigurationRepository,
        pluginActionDefinitionRepository: PluginActionDefinitionRepository,
        pluginProcessLinkRepository: PluginProcessLinkRepository,
        @Lazy pluginFactories: List<PluginFactory<*>>,
        objectMapper: ObjectMapper,
        valueResolverService: ValueResolverService,
        pluginConfigurationSearchRepository: PluginConfigurationSearchRepository,
        validator: Validator
    ): PluginService {
        return PluginService(
            pluginDefinitionRepository,
            pluginConfigurationRepository,
            pluginActionDefinitionRepository,
            pluginProcessLinkRepository,
            pluginFactories,
            objectMapper,
            valueResolverService,
            pluginConfigurationSearchRepository,
            validator,
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun pluginConfigurationSearchRepository(entityManager: EntityManager): PluginConfigurationSearchRepository {
        return PluginConfigurationSearchRepository(entityManager)
    }

    @Bean
    @ConditionalOnMissingBean(PluginDefinitionResource::class)
    fun pluginDefinitionResource(
        pluginService: PluginService
    ): PluginDefinitionResource {
        return PluginDefinitionResource(pluginService)
    }

    @Bean
    @ConditionalOnMissingBean(PluginConfigurationResource::class)
    fun pluginConfigurationResource(
        pluginService: PluginService
    ): PluginConfigurationResource {
        return PluginConfigurationResource(pluginService)
    }

    @Bean
    @ConditionalOnMissingBean(PluginProcessLinkRepository::class)
    fun pluginProcessLinkRepository(
        pluginProcessLinkRepositoryImpl: PluginProcessLinkRepositoryImpl
    ): PluginProcessLinkRepository {
        return PluginProcessLinkRepository(pluginProcessLinkRepositoryImpl)
    }

    @Bean
    fun propertyEncryptionService(
        @Value("\${valtimo.plugin.encryption-secret}")
        secret: String
    ): EncryptionService {
        return EncryptionService(secret)
    }

    @Bean
    @ConditionalOnMissingBean(PluginAutoDeploymentEventListener::class)
    fun pluginAutoDeploymentEventListener(
        objectMapper: ObjectMapper,
        pluginService: PluginService,
        resourceLoader: ResourceLoader
    ): PluginAutoDeploymentEventListener{
        return PluginAutoDeploymentEventListener(
            pluginService = pluginService,
            objectMapper = objectMapper,
            resourceLoader = resourceLoader
        )
    }
}

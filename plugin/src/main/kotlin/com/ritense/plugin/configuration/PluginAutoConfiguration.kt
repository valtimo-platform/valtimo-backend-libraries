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

package com.ritense.plugin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.PluginCategoryResolver
import com.ritense.plugin.PluginDefinitionResolver
import com.ritense.plugin.PluginDeploymentListener
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginActionPropertyDefinitionRepository
import com.ritense.plugin.repository.PluginCategoryRepository
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.repository.PluginPropertyRepository
import com.ritense.plugin.security.config.PluginHttpSecurityConfigurer
import com.ritense.plugin.service.EncryptionService
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.PluginDefinitionResource
import com.ritense.valueresolver.ValueResolverService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        PluginDefinitionRepository::class,
        PluginConfigurationRepository::class,
        PluginActionDefinitionRepository::class,
        PluginProcessLinkRepository::class,
        PluginPropertyRepository::class,
        PluginActionPropertyDefinitionRepository::class,
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
    fun pluginDefinitionResolver(): PluginDefinitionResolver {
        return PluginDefinitionResolver()
    }

    @Bean
    fun pluginCategoryResolver(): PluginCategoryResolver {
        return PluginCategoryResolver()
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
        pluginFactories: List<PluginFactory<*>>,
        objectMapper: ObjectMapper,
        valueResolverService: ValueResolverService
    ): PluginService {
        return PluginService(pluginDefinitionRepository,
            pluginConfigurationRepository,
            pluginActionDefinitionRepository,
            pluginProcessLinkRepository,
            pluginFactories,
            objectMapper,
            valueResolverService
        )
    }

    @Bean
    @ConditionalOnMissingBean(PluginDefinitionResource::class)
    fun pluginDefinitionResource(
        pluginService: PluginService
    ): PluginDefinitionResource {
        return PluginDefinitionResource(pluginService)
    }

    @Bean
    fun propertyEncryptionService(
        @Value("\${valtimo.plugin.encryption-secret}")
        secret: String
    ): EncryptionService {
        return EncryptionService(secret)
    }
}

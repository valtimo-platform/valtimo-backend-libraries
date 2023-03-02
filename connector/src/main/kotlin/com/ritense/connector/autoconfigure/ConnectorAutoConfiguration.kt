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

package com.ritense.connector.autoconfigure

import com.ritense.connector.autodeployment.ConnectorApplicationReadyEventListener
import com.ritense.connector.config.AesEncryption
import com.ritense.connector.config.ObjectMapperDependencyFixer
import com.ritense.connector.config.ObjectMapperHolder
import com.ritense.connector.config.SpringContextHelper
import com.ritense.connector.config.SpringHandlerInstantiatorImpl
import com.ritense.connector.domain.Connector
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.repository.ConnectorTypeRepository
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorFluentBuilder
import com.ritense.connector.service.ConnectorService
import com.ritense.connector.web.rest.impl.ConnectorResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.connector.repository"])
@EntityScan("com.ritense.connector.domain")
class ConnectorAutoConfiguration {

    //Services
    @Bean
    fun springHandlerInstantiatorImpl(beanFactory: AutowireCapableBeanFactory): SpringHandlerInstantiatorImpl {
        return SpringHandlerInstantiatorImpl(beanFactory)
    }

    @Bean
    fun springContextHelper(): SpringContextHelper {
        return SpringContextHelper()
    }

    @Bean
    fun objectMapperDependencyFixer(): ObjectMapperDependencyFixer {
        return ObjectMapperDependencyFixer()
    }

    @Bean
    fun objectMapperHolder(springHandlerInstantiatorImpl: SpringHandlerInstantiatorImpl): ObjectMapperHolder {
        return ObjectMapperHolder(springHandlerInstantiatorImpl)
    }

    @Bean
    @ConditionalOnMissingBean(ConnectorService::class)
    fun connectorService(
        applicationContext: ApplicationContext,
        connectorTypeInstanceRepository: ConnectorTypeInstanceRepository,
        connectorTypeRepository: ConnectorTypeRepository
    ): ConnectorService {
        return ConnectorService(applicationContext, connectorTypeInstanceRepository, connectorTypeRepository)
    }

    @Bean
    @ConditionalOnMissingBean(ConnectorDeploymentService::class)
    fun connectorDeploymentService(connectorTypeRepository: ConnectorTypeRepository): ConnectorDeploymentService {
        return ConnectorDeploymentService(connectorTypeRepository)
    }

    @Bean
    @ConditionalOnMissingBean(ConnectorApplicationReadyEventListener::class)
    fun connectorApplicationReadyEventListener(
        connectorDeploymentService: ConnectorDeploymentService,
        connectors: List<Connector>
    ): ConnectorApplicationReadyEventListener {
        return ConnectorApplicationReadyEventListener(connectorDeploymentService, connectors)
    }

    @Bean
    @ConditionalOnMissingBean(ConnectorResource::class)
    fun connectorResource(connectorService: ConnectorService): ConnectorResource {
        return ConnectorResource(connectorService)
    }

    @Bean
    @ConditionalOnMissingBean(ConnectorFluentBuilder::class)
    fun connectorFluentBuilder(connectorService: ConnectorService): ConnectorFluentBuilder {
        return ConnectorFluentBuilder(connectorService)
    }

    @Bean
    @ConditionalOnMissingBean(AesEncryption::class)
    fun aesEncryption(@Value("\${valtimo.connector-encryption.secret}") secret: String): AesEncryption {
        return AesEncryption(secret)
    }
}
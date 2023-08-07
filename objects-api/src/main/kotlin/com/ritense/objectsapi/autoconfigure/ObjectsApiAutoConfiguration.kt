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

package com.ritense.objectsapi.autoconfigure

import com.ritense.connector.domain.Connector
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.service.ConnectorFluentBuilder
import com.ritense.connector.service.ConnectorService
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.domain.sync.listener.DocumentEventListener
import com.ritense.objectsapi.repository.ObjectSyncConfigRepository
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objectsapi.service.ObjectsApiConnector
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.objectsapi.web.rest.impl.ObjectSyncConfigResource
import com.ritense.tenancy.TenantResolver
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.objectsapi.repository"])
@EntityScan("com.ritense.objectsapi.domain")
class ObjectsApiAutoConfiguration {

    //Services

    @Bean
    @ConditionalOnMissingBean(ObjectSyncService::class)
    fun objectSyncService(
        objectSyncConfigRepository: ObjectSyncConfigRepository,
        connectorTypeInstanceRepository: ConnectorTypeInstanceRepository
    ): ObjectSyncService {
        return ObjectSyncService(objectSyncConfigRepository, connectorTypeInstanceRepository)
    }

    //Connector

    @Bean
    @ConditionalOnMissingBean(ObjectsApiConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun objectsApiConnector(
        objectsApiProperties: ObjectsApiProperties,
        documentService: DocumentService
    ): Connector {
        return ObjectsApiConnector(objectsApiProperties, documentService)
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun objectsApiProperties(
    ): ObjectsApiProperties {
        return ObjectsApiProperties()
    }

    //Listener
    @Bean
    @ConditionalOnMissingBean(DocumentEventListener::class)
    fun documentEventListener(
        objectSyncService: ObjectSyncService,
        connectorService: ConnectorService,
        connectorFluentBuilder: ConnectorFluentBuilder,
        documentService: DocumentService,
        tenantResolver: TenantResolver
    ): DocumentEventListener {
        return DocumentEventListener(
            objectSyncService,
            connectorService,
            connectorFluentBuilder,
            documentService,
            tenantResolver
        )
    }

    //Resource
    @Bean
    @ConditionalOnMissingBean(ObjectSyncConfigResource::class)
    fun objectSyncConfigResource(objectSyncService: ObjectSyncService): ObjectSyncConfigResource {
        return ObjectSyncConfigResource(objectSyncService)
    }
}

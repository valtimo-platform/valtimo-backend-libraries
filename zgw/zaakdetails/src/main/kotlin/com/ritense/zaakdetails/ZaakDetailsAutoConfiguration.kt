/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.zaakdetails

import com.ritense.document.service.DocumentService
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncManagementResource
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncRepository
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncService
import com.ritense.zaakdetails.security.ZaakDetailsHttpSecurityConfigurer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@EnableJpaRepositories(basePackageClasses = [DocumentObjectenApiSyncRepository::class])
@EntityScan("com.ritense.zaakdetails.documentobjectenapisync")
@AutoConfiguration
class ZaakDetailsAutoConfiguration {

    @Order(Ordered.HIGHEST_PRECEDENCE + 33)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["zaakDetailsLiquibaseMasterChangeLogLocation"])
    fun zaakDetailsLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/zaakdetails-master.xml")
    }

    @Bean
    @ConditionalOnMissingBean(DocumentObjectenApiSyncService::class)
    fun documentObjectenApiSyncService(
        documentObjectenApiSyncRepository: DocumentObjectenApiSyncRepository,
        objectObjectManagementInfoProvider: ObjectManagementInfoProvider,
        documentService: DocumentService,
        pluginService: PluginService,
        objectSyncService: ObjectSyncService,
    ): DocumentObjectenApiSyncService {
        return DocumentObjectenApiSyncService(
            documentObjectenApiSyncRepository,
            objectObjectManagementInfoProvider,
            documentService,
            pluginService,
            objectSyncService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(DocumentObjectenApiSyncManagementResource::class)
    fun documentObjectenApiSyncManagementResource(
        documentObjectenApiSyncService: DocumentObjectenApiSyncService,
        objectManagementInfoProvider: ObjectManagementInfoProvider,
    ): DocumentObjectenApiSyncManagementResource {
        return DocumentObjectenApiSyncManagementResource(
            documentObjectenApiSyncService,
            objectManagementInfoProvider,
        )
    }

    @Order(400)
    @Bean
    @ConditionalOnMissingBean(ZaakDetailsHttpSecurityConfigurer::class)
    fun zaakDetailsHttpSecurityConfigurer(): ZaakDetailsHttpSecurityConfigurer {
        return ZaakDetailsHttpSecurityConfigurer()
    }
}

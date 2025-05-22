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

package com.ritense.document.autoconfiguration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.security.DocumentMigrationHttpSecurityConfigurer
import com.ritense.document.service.DocumentMigrationService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.document.web.rest.DocumentMigrationManagementResource
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order

@AutoConfiguration
class DocumentMigrationAutoConfiguration {
    @Order(294)
    @Bean
    @ConditionalOnMissingBean(DocumentMigrationHttpSecurityConfigurer::class)
    fun documentMigrationHttpSecurityConfigurer(): DocumentMigrationHttpSecurityConfigurer {
        return DocumentMigrationHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(DocumentMigrationManagementResource::class)
    fun documentMigrationManagementResource(
        internalCaseService: DocumentMigrationService,
    ): DocumentMigrationManagementResource {
        return DocumentMigrationManagementResource(internalCaseService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentMigrationService::class)
    fun documentMigrationService(
        documentDefinitionService: JsonSchemaDocumentDefinitionService,
        documentRepository: JsonSchemaDocumentRepository,
        applicationContext: ApplicationContext,
        objectMapper: ObjectMapper,
    ): DocumentMigrationService {
        return DocumentMigrationService(
            documentDefinitionService,
            documentRepository,
            applicationContext,
            objectMapper
        )
    }

}
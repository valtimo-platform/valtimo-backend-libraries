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

import com.ritense.document.DocumentDocumentDefinitionMapper
import com.ritense.document.JsonSchemaDocumentDefinitionSpecificationFactory
import com.ritense.document.JsonSchemaDocumentSpecificationFactory
import com.ritense.document.SearchFieldSpecificationFactory
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.repository.DocumentDefinitionRepository
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy

@AutoConfiguration
class DocumentAuthorizationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(JsonSchemaDocumentSpecificationFactory::class)
    fun jsonSchemaDocumentSpecificationFactory(
        documentRepository: JsonSchemaDocumentRepository,
        queryDialectHelper: QueryDialectHelper
    ) = JsonSchemaDocumentSpecificationFactory(documentRepository, queryDialectHelper)

    @Bean
    @ConditionalOnMissingBean(JsonSchemaDocumentDefinitionSpecificationFactory::class)
    fun jsonSchemaDocumentDefinitionSpecificationFactory(
        queryDialectHelper: QueryDialectHelper,
        @Lazy documentDefinitionService: JsonSchemaDocumentDefinitionService
    ) = JsonSchemaDocumentDefinitionSpecificationFactory(queryDialectHelper, documentDefinitionService)

    @Bean
    @ConditionalOnMissingBean(SearchFieldSpecificationFactory::class)
    fun searchFieldSpecificationFactory(
        queryDialectHelper: QueryDialectHelper
    ) = SearchFieldSpecificationFactory(queryDialectHelper)

    @Bean
    fun documentDocumentDefinitionMapper(
        documentDefinitionRepository: DocumentDefinitionRepository<JsonSchemaDocumentDefinition>
    ) = DocumentDocumentDefinitionMapper(documentDefinitionRepository)
}
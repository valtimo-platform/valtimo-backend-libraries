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

package com.ritense.document.autoconfiguration

import com.ritense.document.JsonSchemaDocumentDefinitionSpecificationFactory
import com.ritense.document.JsonSchemaDocumentSnapshotSpecificationFactory
import com.ritense.document.JsonSchemaDocumentSpecificationFactory
import com.ritense.document.SearchFieldSpecificationFactory
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class DocumentAuthorizationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(JsonSchemaDocumentSpecificationFactory::class)
    fun jsonSchemaDocumentSpecificationFactory(
        @Lazy documentService: JsonSchemaDocumentService,
        queryDialectHelper: QueryDialectHelper
    ) = JsonSchemaDocumentSpecificationFactory(documentService, queryDialectHelper)

    @Bean
    @ConditionalOnMissingBean(JsonSchemaDocumentDefinitionSpecificationFactory::class)
    fun jsonSchemaDocumentDefinitionSpecificationFactory(
        queryDialectHelper: QueryDialectHelper
    ) = JsonSchemaDocumentDefinitionSpecificationFactory(queryDialectHelper)

    @Bean
    @ConditionalOnMissingBean(JsonSchemaDocumentSnapshotSpecificationFactory::class)
    fun jsonSchemaDocumentSnapshotSpecificationFactory(
        queryDialectHelper: QueryDialectHelper
    ) = JsonSchemaDocumentSnapshotSpecificationFactory(queryDialectHelper)

    @Bean
    @ConditionalOnMissingBean(SearchFieldSpecificationFactory::class)
    fun searchFieldSpecificationFactory(
        queryDialectHelper: QueryDialectHelper
    ) = SearchFieldSpecificationFactory(queryDialectHelper)
}
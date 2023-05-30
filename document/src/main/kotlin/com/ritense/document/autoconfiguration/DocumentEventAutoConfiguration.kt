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

import com.ritense.authorization.AuthorizationSpecificationFactory
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.document.JsonSchemaDocumentSpecificationFactory
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.listener.DocumentEventListener
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DocumentEventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DocumentEventListener::class)
    fun documentCreatedEventListener(sseSubscritionService: SseSubscriptionService): DocumentEventListener {
        return DocumentEventListener(sseSubscritionService)
    }

    @Bean
    @ConditionalOnMissingBean(JsonSchemaDocumentSpecificationFactory::class)
    fun jsonSchemaDocumentSpecificationFactory(
        queryDialectHelper: QueryDialectHelper
    ): AuthorizationSpecificationFactory<JsonSchemaDocument> {
        return JsonSchemaDocumentSpecificationFactory(queryDialectHelper)
    }
}
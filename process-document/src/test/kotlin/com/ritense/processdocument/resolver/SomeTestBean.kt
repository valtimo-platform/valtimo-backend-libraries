/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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
package com.ritense.processdocument.resolver

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SomeTestBean(
    private val documentValueResolver: LockedDocumentJsonValueResolverFactory,
    private val documentService: DocumentService
) {
    fun updateDocument(document: Document) {
        runWithoutAuthorization {
            documentValueResolver.handleValues(document.id().id, mapOf("doc-locked:/street" to "anotherStreet"))
        }
    }

    @Transactional
    fun findAndUpdateDocument(document: Document) {
        runWithoutAuthorization {
            val documentRetrieved = documentService.get(document.id().id.toString())
            logger.info("Getting document ${document.hashCode()}")

            updateDocument(documentRetrieved)
        }
    }

    companion object {
        private val logger = mu.KotlinLogging.logger { }
    }
}

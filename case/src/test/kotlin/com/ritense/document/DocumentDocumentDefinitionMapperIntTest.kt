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

package com.ritense.document

import com.ritense.BaseIntegrationTest
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW_LIST
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentDocumentDefinitionMapperIntTest @Autowired constructor(
    private val authorizationService: AuthorizationService,
) : BaseIntegrationTest() {

    @BeforeEach
    override fun beforeEachBase() {
    }

    @AfterEach
    fun afterEach() {
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = ["ROLE_DUPLICATE_TEST"])
    fun `should not get duplicate documents`() {
        val documentDefinition = definitionOf("person")
        val documents = runWithoutAuthorization {
            listOf(
                createDocument(documentDefinition, """{"firstName":"James"}"""),
                createDocument(documentDefinition, """{"firstName":"Asha"}"""),
                createDocument(documentDefinition, """{"firstName":"Morgan"}"""),
            )
        }

        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                JsonSchemaDocument::class.java,
                VIEW_LIST
            ),
            null
        )

        val count = documentRepository.count(spec)

        assertEquals(3, count)
        documentRepository.deleteAll(documents)
    }
}

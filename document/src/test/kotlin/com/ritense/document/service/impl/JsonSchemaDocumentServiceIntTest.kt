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

package com.ritense.document.service.impl

import com.ritense.document.BaseIntegrationTest
import com.ritense.document.WithMockTenantUser
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import javax.transaction.Transactional

@Transactional
class JsonSchemaDocumentServiceIntTest : BaseIntegrationTest() {

    @Test
    @Order(1)
    @WithMockTenantUser
    fun `should create document`() {

        val content = JsonDocumentContent(
            """
            {"street" : "Funenpark"}
            """
        )
        val newDocumentRequest = NewDocumentRequest(
            "house",
            content.asJson()
        ).withTenantId(TENANT_ID)

        val resultInsert = documentService.createDocument(newDocumentRequest)
        val documentInserted = resultInsert.resultingDocument().get() as JsonSchemaDocument

        assertThat(documentInserted.content().asJson()).isEqualTo(newDocumentRequest.content())
        assertThat(documentInserted.sequence()).isNotNull
        assertThat(documentInserted.createdBy()).isEqualTo(USERNAME)
        assertThat(documentInserted.modifiedOn()).isEmpty()
        assertThat(documentInserted.tenantId()).isEqualTo(TENANT_ID)
    }

    @Test
    @Order(2)
    @WithMockTenantUser
    fun `should update document`() {
        val content = JsonDocumentContent(
            """
            {"street" : "Funenpark 2"}
            """
        )
        val newDocumentRequest = NewDocumentRequest(
            "house",
            content.asJson()
        ).withTenantId(TENANT_ID)

        val resultInsert = documentService.createDocument(newDocumentRequest)
        val documentInserted = resultInsert.resultingDocument().get() as JsonSchemaDocument

        assertThat(documentInserted.content().asJson()).isEqualTo(newDocumentRequest.content())
        assertThat(documentInserted.sequence()).isNotNull
        assertThat(documentInserted.createdBy()).isEqualTo(USERNAME)
        assertThat(documentInserted.createdOn()).isNotNull
        assertThat(documentInserted.modifiedOn()).isEmpty()
        assertThat(documentInserted.tenantId()).isEqualTo(TENANT_ID)

        val modifyRequest = ModifyDocumentRequest(
            documentInserted.id.id.toString(),
            JsonDocumentContent(
                """
                {"street" : "new street"}
            """
            ).asJson(),
            documentInserted.version().toString()
        ).withTenantId(TENANT_ID)

        val result = documentService.modifyDocument(modifyRequest)
        val documentModified = result.resultingDocument().get() as JsonSchemaDocument

        assertThat(documentModified.content().asJson()).isEqualTo(modifyRequest.content())
        assertThat(documentModified.sequence()).isNotNull
        assertThat(documentModified.createdBy()).isEqualTo(USERNAME)
        assertThat(documentModified.modifiedOn()).isPresent()
        assertThat(documentInserted.tenantId()).isEqualTo(TENANT_ID)
    }

}
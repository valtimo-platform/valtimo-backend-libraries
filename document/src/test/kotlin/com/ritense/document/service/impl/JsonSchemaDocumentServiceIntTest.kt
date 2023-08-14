package com.ritense.document.service.impl

import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.tenancy.authentication.TenantAuthenticationToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

class JsonSchemaDocumentServiceIntTest : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
        setupAuth()
    }

    @AfterEach
    fun tearDown() {
        cleanAuth()
    }

    @Test
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
        assertThat(documentInserted.sequence()).isEqualTo(1)
        assertThat(documentInserted.createdBy()).isEqualTo(USERNAME)
        assertThat(documentInserted.modifiedOn()).isEmpty()
    }

    @Test
    fun `should update document`() {
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
        assertThat(documentInserted.sequence()).isEqualTo(2)
        assertThat(documentInserted.createdBy()).isEqualTo(USERNAME)
        assertThat(documentInserted.createdOn()).isNotNull
        assertThat(documentInserted.modifiedOn()).isEmpty()

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
        assertThat(documentModified.sequence()).isEqualTo(1)
        assertThat(documentModified.createdBy()).isEqualTo(USERNAME)
        assertThat(documentModified.modifiedOn()).isPresent()
    }

    fun setupAuth() {
        val principal = User(USERNAME, "", listOf(SimpleGrantedAuthority("ROLE_USER")))
        SecurityContextHolder.getContext().authentication = TenantAuthenticationToken(
            delegate = UsernamePasswordAuthenticationToken(
                principal, null
            ),
            tenantId = "1",
            fullName = "Test User"
        )
    }

    fun cleanAuth() {
        SecurityContextHolder.getContext().authentication = null
    }
}
package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.formviewmodel.BaseIntegrationTest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional

@Transactional
class ProcessAuthorizationServiceIntTest @Autowired constructor(
    private val processAuthorizationService: ProcessAuthorizationService,
    private val documentService: JsonSchemaDocumentService,
    private val objectMapper: ObjectMapper,
) : BaseIntegrationTest() {

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should fail validation for process definition`() {
        assertThrows<AccessDeniedException> {
            processAuthorizationService.checkStartProcessAuthorization("fvm-uicomponent-task-process")
        }
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should not fail validation for process definition without context`() {
        processAuthorizationService.checkStartProcessAuthorization("fvm-uicomponent-task-process")
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should fail validation for process definition with document context`() {
        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "fvm",
                    objectMapper.createObjectNode()
                )
            )
        }.resultingDocument().get()

        assertThrows<AccessDeniedException> {
            processAuthorizationService.checkStartProcessAuthorization("fvm-form-task-process", document)
        }
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should not fail validation for process definition with document context`() {
        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "fvm",
                    objectMapper.readTree(
                    """{"allow": true}""".trimIndent())
                )
            )
        }.resultingDocument().get()

        processAuthorizationService.checkStartProcessAuthorization("fvm-form-task-process", document)
    }
}
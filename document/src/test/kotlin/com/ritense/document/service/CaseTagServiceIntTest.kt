package com.ritense.document.service

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.BaseTest
import com.ritense.document.domain.CaseTagColor
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional


@Transactional
class CaseTagServiceIntTest @Autowired constructor(
    private val caseTagService: CaseTagService
) : BaseIntegrationTest() {

    @Test
    fun `should create a case tag`() {

        val request = CaseTagCreateRequestDto(
            key = "some-tag",
            title = "Some Tag",
            color = CaseTagColor.COOLGRAY,
            order = 1
        )

        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create("house", request)
        }

        assertEquals(CaseTagColor.COOLGRAY, caseTagService.get("house", "some-tag" ).color)
    }

    @Test
    fun `should throw error when creating status with invalid key`() {
        AuthorizationContext.runWithoutAuthorization {
            val exception = assertThrows<ConstraintViolationException> {
                caseTagService.create(
                    "house",
                    CaseTagCreateRequestDto(
                        key = "<this-is-not-a-valid-tag#>",
                        title = "Some Tag",
                        color = CaseTagColor.COOLGRAY,
                        order = 1
                    )
                )
            }
            kotlin.test.assertEquals("""create.request.key: must match "[a-z][a-z0-9-_]+"""", exception.message)
        }
    }

    @Test
    fun `should update tag for existing tag`() {
        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(
                "house",
                CaseTagCreateRequestDto(
                    key = "some-tag",
                    title = "Some Tag",
                    color = CaseTagColor.COOLGRAY,
                    order = 1
                )
            )

            caseTagService.update(
                "house",
                "some-tag",
                CaseTagUpdateRequestDto(
                    key = "some-tag",
                    title = "New Title",
                    color = CaseTagColor.BLUE,
                    order = 1
                )
            )

            val updatedTag = caseTagService.get("house","some-tag" )

            assertEquals(CaseTagColor.BLUE ,updatedTag.color)
            assertEquals("New Title" ,updatedTag.title)
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should add case tag `() {

        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(
                "house",
                CaseTagCreateRequestDto("some-tag", "Some Tag", CaseTagColor.MAGENTA, 1))
        }

        val content = JsonDocumentContent("{\"street\": \"Funenpark\"}")
        val document = JsonSchemaDocument.create(
            definition(),
            content,
            BaseTest.USERNAME,
            documentSequenceGeneratorService,
            null
        ).resultingDocument().get()

        documentRepository.save(document)

        documentService.addCaseTag(document.id, "some-tag")

        val updatedDocument = documentService.findBy(document.id).get()

        println(updatedDocument.caseTags())

    }

    companion object {
        private const val USERNAME = "john@ritense.com"
    }
}
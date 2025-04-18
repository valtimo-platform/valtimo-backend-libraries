package com.ritense.document.service

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.BaseTest
import com.ritense.document.domain.CaseTagColor
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.exception.CaseTagAlreadyExistsException
import com.ritense.document.exception.CaseTagNotFoundException
import com.ritense.document.repository.CaseTagRepository
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.semver4j.Semver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@Transactional
class CaseTagServiceIntTest @Autowired constructor(
    private val caseTagService: CaseTagService,
    private val caseTagRepository: CaseTagRepository
) : BaseIntegrationTest() {

    @Test
    fun `should create a case tag`() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")

        val request = CaseTagCreateRequestDto(
            key = "some-tag",
            title = "Some Tag",
            color = CaseTagColor.COOLGRAY
        )

        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(caseDefinitionId, request)
        }

        val caseTag = caseTagRepository
            .findDistinctByIdCaseDefinitionIdAndIdKey(caseDefinitionId, "some-tag")

        val caseTagCount = caseTagRepository
            .findByIdCaseDefinitionIdOrderByOrder(caseDefinitionId).size

        assertNotNull(caseTag)
        assertEquals("house", caseTag.id.caseDefinitionId.key)
        assertEquals(Semver("1.0.0"), caseTag.id.caseDefinitionId.versionTag)
        assertEquals("some-tag", caseTag.id.key)
        assertEquals("Some Tag", caseTag.title)
        assertEquals(caseTagCount - 1, caseTag.order)
    }

    @Test
    fun `should throw error when creating status with invalid key`() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        AuthorizationContext.runWithoutAuthorization {
            val exception = assertThrows<ConstraintViolationException> {
                caseTagService.create(
                    caseDefinitionId,
                    CaseTagCreateRequestDto(
                        key = "<this-is-not-a-valid-tag#>",
                        title = "Some Tag",
                        color = CaseTagColor.COOLGRAY
                    )
                )
            }
            assertEquals("""create.request.key: must match "[a-z][a-z0-9-_]+"""", exception.message)
        }
    }

    @Test
    fun `should not create case tag without proper permissions`() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        assertThrows<AccessDeniedException> {
            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag",
                    title = "Some Tag",
                    color = CaseTagColor.COOLGRAY
                )
            )
        }
    }

    @Test
    fun `should not create case tag for missing definition`() {
        val caseDefinitionId = CaseDefinitionId.of("case-definition-that-does-not-exist", "1.0.0")
        assertThrows<NoSuchElementException> {
            AuthorizationContext.runWithoutAuthorization {
                caseTagService.create(
                    caseDefinitionId,
                    CaseTagCreateRequestDto(
                        key = "some-tag",
                        title = "Some Tag",
                        color = CaseTagColor.COOLGRAY
                    )
                )
            }
        }
    }

    @Test
    fun shouldNotCreateTagForWhenTagAlreadyExists() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag",
                    title = "Some Tag",
                    color = CaseTagColor.COOLGRAY
                )
            )

            assertThrows<CaseTagAlreadyExistsException> {
                caseTagService.create(
                    caseDefinitionId,
                    CaseTagCreateRequestDto(
                        key = "some-tag",
                        title = "Some Tag",
                        color = CaseTagColor.COOLGRAY
                    )
                )
            }
        }
    }

    @Test
    fun `should update tag for existing tag`() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag",
                    title = "Some Tag",
                    color = CaseTagColor.COOLGRAY
                )
            )

            caseTagService.update(
                caseDefinitionId,
                "some-tag",
                CaseTagUpdateRequestDto(
                    key = "some-tag",
                    title = "New Title",
                    color = CaseTagColor.BLUE
                )
            )

            val updatedCaseTag = caseTagRepository
                .findDistinctByIdCaseDefinitionIdAndIdKey(caseDefinitionId, "some-tag")

            val caseTagCount = caseTagRepository
                .findByIdCaseDefinitionIdOrderByOrder(caseDefinitionId).size

            assertNotNull(updatedCaseTag)
            assertEquals("house", updatedCaseTag.id.caseDefinitionId.key)
            assertEquals(Semver("1.0.0"), updatedCaseTag.id.caseDefinitionId.versionTag)
            assertEquals("some-tag", updatedCaseTag.id.key)
            assertEquals("New Title", updatedCaseTag.title)
            assertEquals(caseTagCount - 1, updatedCaseTag.order)
        }
    }

    @Test
    fun shouldNotUpdateTagForMissingTag() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        assertThrows<CaseTagNotFoundException> {
            AuthorizationContext.runWithoutAuthorization {
                caseTagService.update(
                    caseDefinitionId,
                    "some-tag",
                    CaseTagUpdateRequestDto(
                        key = "some-tag",
                        title = "New Title",
                        color = CaseTagColor.BLUE
                    )
                )
            }
        }
    }

    @Test
    fun shouldNotUpdateTagWithoutProperPermissions() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        assertThrows<AccessDeniedException> {
            caseTagService.update(
                caseDefinitionId,
                "some-tag",
                CaseTagUpdateRequestDto(
                    key = "some-tag",
                    title = "New Title",
                    color = CaseTagColor.BLUE
                )
            )
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should add case tag`() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")

        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    "some-tag",
                    "Some Tag",
                    CaseTagColor.MAGENTA
                )
            )
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

    @Test
    fun shouldReorderTagsForExistingTags() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        AuthorizationContext.runWithoutAuthorization {
            caseTagRepository.deleteAll()

            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag-1",
                    title = "Some Tag 1",
                    color = CaseTagColor.COOLGRAY
                )
            )

            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag-2",
                    title = "Some Tag 2",
                    color = CaseTagColor.COOLGRAY
                )
            )

            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag-3",
                    title = "Some Tag 3",
                    color = CaseTagColor.COOLGRAY
                )
            )
        }

        val caseTags = caseTagRepository
            .findByIdCaseDefinitionIdOrderByOrder(caseDefinitionId)

        kotlin.test.assertEquals(3, caseTags.size)
        kotlin.test.assertEquals("some-tag-1", caseTags[0].id.key)
        kotlin.test.assertEquals(caseTags.size - 3, caseTags[0].order)
        kotlin.test.assertEquals("some-tag-2", caseTags[1].id.key)
        kotlin.test.assertEquals(caseTags.size - 2, caseTags[1].order)
        kotlin.test.assertEquals("some-tag-3", caseTags[2].id.key)
        kotlin.test.assertEquals(caseTags.size - 1, caseTags[2].order)

        AuthorizationContext.runWithoutAuthorization {
            caseTagService.update(
                caseDefinitionId,
                listOf(
                    CaseTagUpdateRequestDto(
                        key = "some-tag-1",
                        title = "New Title 1",
                        color = CaseTagColor.BLUE
                    ),
                    CaseTagUpdateRequestDto(
                        key = "some-tag-3",
                        title = "New Title 3",
                        color = CaseTagColor.BLUE
                    ),
                    CaseTagUpdateRequestDto(
                        key = "some-tag-2",
                        title = "New Title 2",
                        color = CaseTagColor.BLUE
                    ),
                )
            )
        }

        val postUpdateCaseTags = caseTagRepository
            .findByIdCaseDefinitionIdOrderByOrder(caseDefinitionId)

        kotlin.test.assertEquals(3, postUpdateCaseTags.size)
        kotlin.test.assertEquals("some-tag-1", postUpdateCaseTags[0].id.key)
        kotlin.test.assertEquals(postUpdateCaseTags.size - 3, postUpdateCaseTags[0].order)
        kotlin.test.assertEquals("some-tag-3", postUpdateCaseTags[1].id.key)
        kotlin.test.assertEquals(postUpdateCaseTags.size - 2, postUpdateCaseTags[1].order)
        kotlin.test.assertEquals("some-tag-2", postUpdateCaseTags[2].id.key)
        kotlin.test.assertEquals(postUpdateCaseTags.size - 1, postUpdateCaseTags[2].order)
    }

    @Test
    fun shouldNotReorderForIncorrectNumberOfTags() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        AuthorizationContext.runWithoutAuthorization {
            caseTagRepository.deleteAll()

            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag-1",
                    title = "Some Tag 1",
                    color = CaseTagColor.COOLGRAY
                )
            )

            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag-2",
                    title = "Some Tag 2",
                    color = CaseTagColor.COOLGRAY
                )
            )

            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag-3",
                    title = "Some Tag 3",
                    color = CaseTagColor.COOLGRAY
                )
            )
        }

        assertThrows<IllegalStateException> {
            AuthorizationContext.runWithoutAuthorization {
                caseTagService.update(
                    caseDefinitionId,
                    listOf(
                        CaseTagUpdateRequestDto(
                            key = "some-tag-1",
                            title = "New Title 1",
                            color = CaseTagColor.GREEN
                        )
                    )
                )
            }
        }
    }

    @Test
    fun shouldDeleteTagForExistingTag() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(
                caseDefinitionId,
                CaseTagCreateRequestDto(
                    key = "some-tag-1",
                    title = "Some Tag 1",
                    color = CaseTagColor.COOLGRAY
                )
            )
        }

        val initialCaseTag = caseTagRepository
            .findDistinctByIdCaseDefinitionIdAndIdKey(caseDefinitionId, "some-tag-1")

        assertNotNull(initialCaseTag)

        AuthorizationContext.runWithoutAuthorization {
            caseTagService.delete(caseDefinitionId, "some-tag-1")
        }

        val postDeleteInternalCaseTag = caseTagRepository
            .findDistinctByIdCaseDefinitionIdAndIdKey(caseDefinitionId, "some-tag-1")

        assertNull(postDeleteInternalCaseTag)

    }

    @Test
    fun shouldNotDeleteTagForMissingTag() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        assertThrows<CaseTagNotFoundException> {
            AuthorizationContext.runWithoutAuthorization {
                caseTagService.delete(caseDefinitionId, "some-non-existing-tag")
            }
        }
    }

    @Test
    fun shouldNotDeleteTagWithoutProperPermissions() {
        val caseDefinitionId = CaseDefinitionId.of("house", "1.0.0")
        assertThrows<AccessDeniedException> {
            caseTagService.delete(caseDefinitionId, "some-tag")
        }
    }

    companion object {
        private const val USERNAME = "john@ritense.com"
    }
}
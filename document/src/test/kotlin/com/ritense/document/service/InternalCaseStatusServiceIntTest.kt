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

package com.ritense.document.service

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.InternalCaseStatusColor.GRAY
import com.ritense.document.exception.InternalCaseStatusAlreadyExistsException
import com.ritense.document.exception.InternalCaseStatusNotFoundException
import com.ritense.document.repository.InternalCaseStatusRepository
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateOrderRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateRequestDto
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Transactional
class InternalCaseStatusServiceIntTest @Autowired constructor(
    private val internalCaseStatusService: InternalCaseStatusService,
    private val internalCaseStatusRepository: InternalCaseStatusRepository
) : BaseIntegrationTest() {
    @Test
    fun `should have imported two person internal case statuses`() {
        val internalCaseStatuses =
            internalCaseStatusRepository.findByIdCaseDefinitionNameOrderByOrder("person")

        assertEquals(2, internalCaseStatuses.size)
        assertEquals("closed", internalCaseStatuses[0].id.key)
        assertEquals("Closed", internalCaseStatuses[0].title)
        assertFalse(internalCaseStatuses[0].visibleInCaseListByDefault)
        assertEquals("started", internalCaseStatuses[1].id.key)
        assertEquals("Started", internalCaseStatuses[1].title)
        assertTrue(internalCaseStatuses[1].visibleInCaseListByDefault)
    }
    @Test
    fun `should create status for existing definition`() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house123",
                    "456",
                    true,
                    GRAY
                )
            )
        }

        val internalCaseStatus = internalCaseStatusRepository
            .findDistinctByIdCaseDefinitionNameAndIdKey("house", "house123")

        val internalCaseCount = internalCaseStatusRepository
            .findByIdCaseDefinitionNameOrderByOrder("house").size

        assertNotNull(internalCaseStatus)
        assertEquals("house", internalCaseStatus.id.caseDefinitionName)
        assertEquals("house123", internalCaseStatus.id.key)
        assertEquals("456", internalCaseStatus.title)
        assertTrue(internalCaseStatus.visibleInCaseListByDefault)
        assertEquals(internalCaseCount - 1, internalCaseStatus.order)
    }

    @Test
    fun `should throw error when creating status with invalid key`() {
        AuthorizationContext.runWithoutAuthorization {
            val exception = assertThrows<ConstraintViolationException> {
                internalCaseStatusService.create(
                    "house",
                    InternalCaseStatusCreateRequestDto(
                        "@invalid.key&",
                        "456",
                        true,
                        GRAY
                    )
                )
            }
            assertEquals("""create.request.key: must match "[a-z][a-z0-9-_]+"""", exception.message)
        }
    }

    @Test
    fun `should not create status without proper permissions`() {
        assertThrows<AccessDeniedException> {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house123",
                    "456",
                    true,
                    GRAY
                )
            )
        }
    }

    @Test
    fun `should not create status for missing definition`() {
        assertThrows<NoSuchElementException> {
            AuthorizationContext.runWithoutAuthorization {
                internalCaseStatusService.create(
                    "case-definition-that-does-not-exist",
                    InternalCaseStatusCreateRequestDto(
                        "test123",
                        "456",
                        true,
                        GRAY
                    )
                )
            }
        }
    }

    @Test
    fun shouldNotCreateStatusForWhenStatusAlreadyExists() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house123",
                    "456",
                    true,
                    GRAY
                )
            )

            assertThrows<InternalCaseStatusAlreadyExistsException> {
                internalCaseStatusService.create(
                    "house",
                    InternalCaseStatusCreateRequestDto(
                        "house123",
                        "456",
                        true,
                        GRAY
                    )
                )
            }
        }
    }

    @Test
    fun shouldUpdateStatusForExistingStatus() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house123",
                    "456",
                    true,
                    GRAY
                )
            )

            internalCaseStatusService.update(
                "house",
                "house123",
                InternalCaseStatusUpdateRequestDto(
                    "house123",
                    "789",
                    false,
                    GRAY
                )
            )
        }

        val internalCaseStatus = internalCaseStatusRepository
            .findDistinctByIdCaseDefinitionNameAndIdKey("house", "house123")

        val internalCaseCount = internalCaseStatusRepository
            .findByIdCaseDefinitionNameOrderByOrder("house").size

        assertNotNull(internalCaseStatus)
        assertEquals("house", internalCaseStatus.id.caseDefinitionName)
        assertEquals("house123", internalCaseStatus.id.key)
        assertEquals("789", internalCaseStatus.title)
        assertFalse(internalCaseStatus.visibleInCaseListByDefault)
        assertEquals(internalCaseCount - 1, internalCaseStatus.order)

    }

    @Test
    fun shouldNotUpdateStatusForMissingStatus() {
        assertThrows<InternalCaseStatusNotFoundException> {
            AuthorizationContext.runWithoutAuthorization {
                internalCaseStatusService.update(
                    "house",
                    "house123",
                    InternalCaseStatusUpdateRequestDto(
                        "house123",
                        "789",
                        false,
                        GRAY
                    )
                )
            }
        }
    }

    @Test
    fun shouldNotUpdateStatusWithoutProperPermissions() {
        assertThrows<AccessDeniedException> {
            internalCaseStatusService.update(
                "house",
                "house123",
                InternalCaseStatusUpdateRequestDto(
                    "house123",
                    "789",
                    true,
                    GRAY
                )
            )
        }
    }

    @Test
    fun shouldReorderStatusesForExistingStatuses() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusRepository.deleteAll()

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house123",
                    "456",
                    true,
                    GRAY
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house124",
                    "457",
                    true,
                    GRAY
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house125",
                    "458",
                    false,
                    GRAY
                )
            )
        }

        val initialInternalCaseStatuses = internalCaseStatusRepository
            .findByIdCaseDefinitionNameOrderByOrder("house")

        assertEquals(3, initialInternalCaseStatuses.size)
        assertEquals("house123", initialInternalCaseStatuses[0].id.key)
        assertEquals(initialInternalCaseStatuses.size - 3, initialInternalCaseStatuses[0].order)
        assertEquals("house124", initialInternalCaseStatuses[1].id.key)
        assertEquals(initialInternalCaseStatuses.size - 2, initialInternalCaseStatuses[1].order)
        assertEquals("house125", initialInternalCaseStatuses[2].id.key)
        assertEquals(initialInternalCaseStatuses.size - 1, initialInternalCaseStatuses[2].order)

        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.update(
                "house",
                listOf(
                    InternalCaseStatusUpdateOrderRequestDto(
                        "house123",
                        "456",
                        true,
                        GRAY
                    ),
                    InternalCaseStatusUpdateOrderRequestDto(
                        "house125",
                        "458",
                        true,
                        GRAY
                    ),
                    InternalCaseStatusUpdateOrderRequestDto(
                        "house124",
                        "457",
                        true,
                        GRAY
                    )
                )
            )
        }

        val postUpdateInternalCaseStatuses = internalCaseStatusRepository
            .findByIdCaseDefinitionNameOrderByOrder("house")

        assertEquals(3, postUpdateInternalCaseStatuses.size)
        assertEquals("house123", postUpdateInternalCaseStatuses[0].id.key)
        assertTrue(postUpdateInternalCaseStatuses[0].visibleInCaseListByDefault)
        assertEquals(postUpdateInternalCaseStatuses.size - 3, postUpdateInternalCaseStatuses[0].order)
        assertEquals("house125", postUpdateInternalCaseStatuses[1].id.key)
        assertTrue(postUpdateInternalCaseStatuses[1].visibleInCaseListByDefault)
        assertEquals(postUpdateInternalCaseStatuses.size - 2, postUpdateInternalCaseStatuses[1].order)
        assertEquals("house124", postUpdateInternalCaseStatuses[2].id.key)
        assertTrue(postUpdateInternalCaseStatuses[2].visibleInCaseListByDefault)
        assertEquals(postUpdateInternalCaseStatuses.size - 1, postUpdateInternalCaseStatuses[2].order)
    }

    @Test
    fun shouldNotReorderStatusesForIncorrectNumberOfStatuses() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusRepository.deleteAll()

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house123",
                    "456",
                    true,
                    GRAY
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house124",
                    "457",
                    true,
                    GRAY
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house125",
                    "458",
                    false,
                    GRAY
                )
            )
        }

        val initialInternalCaseStatuses = internalCaseStatusRepository
            .findByIdCaseDefinitionNameOrderByOrder("house")

        assertEquals(3, initialInternalCaseStatuses.size)
        assertEquals("house123", initialInternalCaseStatuses[0].id.key)
        assertEquals(initialInternalCaseStatuses.size - 3, initialInternalCaseStatuses[0].order)
        assertEquals("house124", initialInternalCaseStatuses[1].id.key)
        assertEquals(initialInternalCaseStatuses.size - 2, initialInternalCaseStatuses[1].order)
        assertEquals("house125", initialInternalCaseStatuses[2].id.key)
        assertEquals(initialInternalCaseStatuses.size - 1, initialInternalCaseStatuses[2].order)

        assertThrows<IllegalStateException> {
            AuthorizationContext.runWithoutAuthorization {
                internalCaseStatusService.update(
                    "house",
                    listOf(
                        InternalCaseStatusUpdateOrderRequestDto(
                            "house123",
                            "456",
                            true,
                            GRAY
                        )
                    )
                )
            }
        }
    }

    @Test
    fun shouldNotReorderStatusesWithoutProperPermissions() {
        assertThrows<AccessDeniedException> {
            internalCaseStatusService.update(
                "house",
                listOf(
                    InternalCaseStatusUpdateOrderRequestDto(
                        "house123",
                        "789",
                        true,
                        GRAY
                    )
                )
            )
        }
    }

    @Test
    fun shouldDeleteStatusForExistingStatus() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "house123",
                    "456",
                    true,
                    GRAY
                )
            )
        }

        val initialInternalCaseStatus = internalCaseStatusRepository
            .findDistinctByIdCaseDefinitionNameAndIdKey("house", "house123")

        assertNotNull(initialInternalCaseStatus)

        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.delete("house", "house123")
        }

        val postDeleteInternalCaseStatus = internalCaseStatusRepository
            .findDistinctByIdCaseDefinitionNameAndIdKey("house", "house123")

        assertNull(postDeleteInternalCaseStatus)


    }

    @Test
    fun shouldNotDeleteStatusForMissingStatus() {
        assertThrows<InternalCaseStatusNotFoundException> {
            AuthorizationContext.runWithoutAuthorization {
                internalCaseStatusService.delete("house", "house123")
            }
        }
    }

    @Test
    fun shouldNotDeleteStatusWithoutProperPermissions() {
        assertThrows<AccessDeniedException> {
            internalCaseStatusService.delete("house", "house123")
        }
    }
}
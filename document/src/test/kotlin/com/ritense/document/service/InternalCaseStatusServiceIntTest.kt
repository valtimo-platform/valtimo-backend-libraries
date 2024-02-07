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
import com.ritense.document.exception.InternalCaseStatusAlreadyExistsException
import com.ritense.document.exception.InternalCaseStatusNotFoundException
import com.ritense.document.repository.InternalCaseStatusRepository
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateOrderRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateRequestDto
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
    fun shouldCreateStatusForExistingDefinition() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "123",
                    "456",
                    true,
                )
            )
        }

        val internalCaseStatus = internalCaseStatusRepository
            .findDistinctById_CaseDefinitionNameAndId_Key("house", "123")

        val internalCaseCount = internalCaseStatusRepository
            .findById_CaseDefinitionNameOrderByOrder("house").size

        assertNotNull(internalCaseStatus)
        assertEquals("house", internalCaseStatus.id.caseDefinitionName)
        assertEquals("123", internalCaseStatus.id.key)
        assertEquals("456", internalCaseStatus.title)
        assertTrue(internalCaseStatus.visibleInCaseListByDefault)
        assertEquals(internalCaseCount - 1, internalCaseStatus.order)
    }

    @Test
    fun shouldNotCreateStatusWithoutProperPermissions() {
        assertThrows<AccessDeniedException> {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "123",
                    "456",
                    true,
                )
            )
        }
    }

    @Test
    fun shouldNotCreateStatusForMissingDefinition() {
        assertThrows<NoSuchElementException> {
            AuthorizationContext.runWithoutAuthorization {
                internalCaseStatusService.create(
                    "case-definition-that-does-not-exist",
                    InternalCaseStatusCreateRequestDto(
                        "123",
                        "456",
                        true,
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
                    "123",
                    "456",
                    true,
                )
            )

            assertThrows<InternalCaseStatusAlreadyExistsException> {
                internalCaseStatusService.create(
                    "house",
                    InternalCaseStatusCreateRequestDto(
                        "123",
                        "456",
                        true,
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
                    "123",
                    "456",
                    true,
                )
            )

            internalCaseStatusService.update(
                "house",
                "123",
                InternalCaseStatusUpdateRequestDto(
                    "123",
                    "789",
                    false,
                )
            )
        }

        val internalCaseStatus = internalCaseStatusRepository
            .findDistinctById_CaseDefinitionNameAndId_Key("house", "123")

        val internalCaseCount = internalCaseStatusRepository
            .findById_CaseDefinitionNameOrderByOrder("house").size

        assertNotNull(internalCaseStatus)
        assertEquals("house", internalCaseStatus.id.caseDefinitionName)
        assertEquals("123", internalCaseStatus.id.key)
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
                    "123",
                    InternalCaseStatusUpdateRequestDto(
                        "123",
                        "789",
                        false,
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
                "123",
                InternalCaseStatusUpdateRequestDto(
                    "123",
                    "789",
                    true,
                )
            )
        }
    }

    @Test
    fun shouldReorderStatusesForExistingStatuses() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "123",
                    "456",
                    true,
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "124",
                    "457",
                    true,
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "125",
                    "458",
                    false,
                )
            )
        }

        val initialInternalCaseStatuses = internalCaseStatusRepository
            .findById_CaseDefinitionNameOrderByOrder("house")

        assertEquals(3, initialInternalCaseStatuses.size)
        assertEquals("123", initialInternalCaseStatuses[0].id.key)
        assertEquals(initialInternalCaseStatuses.size - 3, initialInternalCaseStatuses[0].order)
        assertEquals("124", initialInternalCaseStatuses[1].id.key)
        assertEquals(initialInternalCaseStatuses.size - 2, initialInternalCaseStatuses[1].order)
        assertEquals("125", initialInternalCaseStatuses[2].id.key)
        assertEquals(initialInternalCaseStatuses.size - 1, initialInternalCaseStatuses[2].order)

        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.update(
                "house",
                listOf(
                    InternalCaseStatusUpdateOrderRequestDto(
                        "123",
                        "456",
                        true
                    ),
                    InternalCaseStatusUpdateOrderRequestDto(
                        "125",
                        "458",
                        true
                    ),
                    InternalCaseStatusUpdateOrderRequestDto(
                        "124",
                        "457",
                        true
                    )
                )
            )
        }

        val postUpdateInternalCaseStatuses = internalCaseStatusRepository
            .findById_CaseDefinitionNameOrderByOrder("house")

        assertEquals(3, postUpdateInternalCaseStatuses.size)
        assertEquals("123", postUpdateInternalCaseStatuses[0].id.key)
        assertTrue(postUpdateInternalCaseStatuses[0].visibleInCaseListByDefault)
        assertEquals(postUpdateInternalCaseStatuses.size - 3, postUpdateInternalCaseStatuses[0].order)
        assertEquals("125", postUpdateInternalCaseStatuses[1].id.key)
        assertTrue(postUpdateInternalCaseStatuses[1].visibleInCaseListByDefault)
        assertEquals(postUpdateInternalCaseStatuses.size - 2, postUpdateInternalCaseStatuses[1].order)
        assertEquals("124", postUpdateInternalCaseStatuses[2].id.key)
        assertTrue(postUpdateInternalCaseStatuses[2].visibleInCaseListByDefault)
        assertEquals(postUpdateInternalCaseStatuses.size - 1, postUpdateInternalCaseStatuses[2].order)
    }

    @Test
    fun shouldNotReorderStatusesForIncorrectNumberOfStatuses() {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "123",
                    "456",
                    true,
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "124",
                    "457",
                    true,
                )
            )

            internalCaseStatusService.create(
                "house",
                InternalCaseStatusCreateRequestDto(
                    "125",
                    "458",
                    false,
                )
            )
        }

        val initialInternalCaseStatuses = internalCaseStatusRepository
            .findById_CaseDefinitionNameOrderByOrder("house")

        assertEquals(3, initialInternalCaseStatuses.size)
        assertEquals("123", initialInternalCaseStatuses[0].id.key)
        assertEquals(initialInternalCaseStatuses.size - 3, initialInternalCaseStatuses[0].order)
        assertEquals("124", initialInternalCaseStatuses[1].id.key)
        assertEquals(initialInternalCaseStatuses.size - 2, initialInternalCaseStatuses[1].order)
        assertEquals("125", initialInternalCaseStatuses[2].id.key)
        assertEquals(initialInternalCaseStatuses.size - 1, initialInternalCaseStatuses[2].order)

        assertThrows<IllegalStateException> {
            AuthorizationContext.runWithoutAuthorization {
                internalCaseStatusService.update(
                    "house",
                    listOf(
                        InternalCaseStatusUpdateOrderRequestDto(
                            "123",
                            "456",
                            true
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
                        "123",
                        "789",
                        true,
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
                    "123",
                    "456",
                    true,
                )
            )
        }

        val initialInternalCaseStatus = internalCaseStatusRepository
            .findDistinctById_CaseDefinitionNameAndId_Key("house", "123")

        assertNotNull(initialInternalCaseStatus)

        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatusService.delete("house", "123")
        }

        val postDeleteInternalCaseStatus = internalCaseStatusRepository
            .findDistinctById_CaseDefinitionNameAndId_Key("house", "123")

        assertNull(postDeleteInternalCaseStatus)


    }

    @Test
    fun shouldNotDeleteStatusForMissingStatus() {
        assertThrows<InternalCaseStatusNotFoundException> {
            AuthorizationContext.runWithoutAuthorization {
                internalCaseStatusService.delete("house", "123")
            }
        }
    }

    @Test
    fun shouldNotDeleteStatusWithoutProperPermissions() {
        assertThrows<AccessDeniedException> {
            internalCaseStatusService.delete("house", "123")
        }
    }
}
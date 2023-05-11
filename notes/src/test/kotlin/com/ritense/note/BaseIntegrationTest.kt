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

package com.ritense.note

import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.note.domain.Note
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder

@SpringBootTest
@ExtendWith(SpringExtension::class, LiquibaseRunnerExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @MockBean
    lateinit var userManagementService: UserManagementService

    @MockBean
    lateinit var mailSender: MailSender

    // TODO: remove authorization service mocking when case support is added
    @MockBean
    lateinit var authorizationService: AuthorizationService

    // TODO: remove entity manager when case support is added
    @Autowired
    lateinit var entityManager: EntityManager

    @BeforeEach
    fun beforeEachBase() {
        // TODO: remove mocking when case support is added
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val authorizationSpecification: AuthorizationSpecification<Note> = mock()

        whenever(authorizationService
            .getAuthorizationSpecification(
                any<AuthorizationRequest<Note>>(),
                anyOrNull()
            )
        ).thenReturn(authorizationSpecification)
        whenever(authorizationSpecification
            .toPredicate(any(), any(), any())
        ).thenReturn(criteriaBuilder.equal(criteriaBuilder.literal(1), 1))
    }
}
